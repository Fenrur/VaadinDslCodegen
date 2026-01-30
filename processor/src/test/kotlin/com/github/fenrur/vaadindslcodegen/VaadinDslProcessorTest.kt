@file:OptIn(ExperimentalCompilerApi::class)

package com.github.fenrur.vaadindslcodegen

import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspProcessorOptions
import com.tschuchort.compiletesting.kspSourcesDir
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class VaadinDslProcessorTest {

    @TempDir
    lateinit var tempDir: File

    private fun compile(
        vararg sources: SourceFile,
        mode: ContainerMode = ContainerMode.QUARKUS
    ): CompilationResult {
        val compilation = KotlinCompilation().apply {
            this.sources = sources.toList() + annotationSources()
            symbolProcessorProviders = mutableListOf(VaadinDslProcessorProvider())
            kspProcessorOptions = mutableMapOf("vaadindsl.mode" to mode.name)
            inheritClassPath = true
            messageOutputStream = System.out
            workingDir = tempDir
        }
        val result = compilation.compile()
        return CompilationResult(result, compilation.kspSourcesDir)
    }

    data class CompilationResult(
        val result: JvmCompilationResult,
        val kspSourcesDir: File
    ) {
        fun generatedFile(name: String): String? {
            return kspSourcesDir.walkTopDown()
                .filter { it.isFile && it.name == name }
                .firstOrNull()
                ?.readText()
        }
    }

    private fun annotationSources(): List<SourceFile> {
        return listOf(
            SourceFile.kotlin(
                "GenDsl.kt",
                """
                package com.github.fenrur.vaadindslcodegen

                @Target(AnnotationTarget.CLASS)
                @Retention(AnnotationRetention.SOURCE)
                annotation class GenDsl
                """.trimIndent()
            ),
            SourceFile.kotlin(
                "GenDslParam.kt",
                """
                package com.github.fenrur.vaadindslcodegen

                @Target(AnnotationTarget.VALUE_PARAMETER)
                @Retention(AnnotationRetention.SOURCE)
                annotation class GenDslParam
                """.trimIndent()
            ),
            SourceFile.kotlin(
                "VaadinDsl.kt",
                """
                package com.github.fenrur.vaadindslcodegen

                @Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
                @DslMarker
                annotation class VaadinDsl
                """.trimIndent()
            ),
            // Mock Vaadin classes
            SourceFile.kotlin(
                "VaadinMocks.kt",
                """
                package com.vaadin.flow.component

                open class Component
                interface HasComponents {
                    fun add(component: Component)
                }
                open class Composite<T : Component> : Component()
                """.trimIndent()
            ),
            SourceFile.kotlin(
                "VaadinButtonMock.kt",
                """
                package com.vaadin.flow.component.button

                import com.vaadin.flow.component.Component

                open class Button(text: String = "") : Component()
                """.trimIndent()
            ),
            // Mock Quarkus classes
            SourceFile.kotlin(
                "QuarkusMocks.kt",
                """
                package io.quarkus.arc

                object Arc {
                    fun container(): ArcContainer = ArcContainer
                }

                object ArcContainer {
                    fun <T> instance(clazz: Class<T>): InstanceHandle<T> = InstanceHandle()
                }

                class InstanceHandle<T> {
                    @Suppress("UNCHECKED_CAST")
                    fun get(): T = null as T
                }

                annotation class Unremovable
                """.trimIndent()
            ),
            SourceFile.kotlin(
                "JakartaMocks.kt",
                """
                package jakarta.enterprise.context

                annotation class ApplicationScoped
                """.trimIndent()
            )
        )
    }

    // ==================== Basic Tests ====================

    @Test
    fun `should generate factory for simple class with GenDsl`() {
        val source = SourceFile.kotlin(
            "SimpleComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class SimpleComponent : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("SimpleComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("class SimpleComponentFactory")
        assertThat(generated).contains("fun create(): SimpleComponent")
        assertThat(generated).contains("@ApplicationScoped")
        assertThat(generated).contains("@Unremovable")
    }

    @Test
    fun `should generate DSL function for Vaadin component`() {
        val source = SourceFile.kotlin(
            "MyButton.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.button.Button

            @GenDsl
            class MyButton : Button()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("MyButtonFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("@VaadinDsl")
        assertThat(generated).contains("fun HasComponents.myButton(")
        assertThat(generated).contains("block: MyButton.() -> Unit = {}")
        assertThat(generated).contains("add(component)")
        assertThat(generated).contains("component.block()")
    }

    // ==================== GenDslParam Tests ====================

    @Test
    fun `should separate GenDslParam from injected params`() {
        val source = SourceFile.kotlin(
            "CustomButton.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.github.fenrur.vaadindslcodegen.GenDslParam
            import com.vaadin.flow.component.button.Button

            class Logger

            @GenDsl
            class CustomButton(
                private val logger: Logger,
                @GenDslParam val text: String
            ) : Button(text)
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("CustomButtonFactory.kt")
        assertThat(generated).isNotNull()

        // Factory should have logger in constructor
        assertThat(generated).contains("class CustomButtonFactory(")
        assertThat(generated).contains("private val logger: Logger")

        // create() should have text parameter
        assertThat(generated).contains("fun create(")
        assertThat(generated).contains("text: String")

        // DSL function should have text parameter
        assertThat(generated).contains("fun HasComponents.customButton(")
        assertThat(generated).contains("text: String,")
    }

    @Test
    fun `should handle multiple GenDslParam parameters`() {
        val source = SourceFile.kotlin(
            "ConfigurableButton.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.github.fenrur.vaadindslcodegen.GenDslParam
            import com.vaadin.flow.component.button.Button

            @GenDsl
            class ConfigurableButton(
                @GenDslParam val text: String,
                @GenDslParam val enabled: Boolean,
                @GenDslParam val width: Int
            ) : Button(text)
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("ConfigurableButtonFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("text: String")
        assertThat(generated).contains("enabled: Boolean")
        assertThat(generated).contains("width: Int")
    }

    // ==================== Mode Tests ====================

    @Test
    fun `should generate Quarkus annotations in QUARKUS mode`() {
        val source = SourceFile.kotlin(
            "QuarkusComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class QuarkusComponent : Component()
            """.trimIndent()
        )

        val compilation = compile(source, mode = ContainerMode.QUARKUS)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("QuarkusComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("@ApplicationScoped")
        assertThat(generated).contains("@Unremovable")
        assertThat(generated).contains("Arc.container().instance(QuarkusComponentFactory::class.java).get()")
    }

    @Test
    fun `should generate Spring annotations in SPRING mode`() {
        // Add Spring mock
        val springMock = SourceFile.kotlin(
            "SpringMocks.kt",
            """
            package org.springframework.stereotype

            annotation class Component
            """.trimIndent()
        )

        val contextHolderMock = SourceFile.kotlin(
            "ContextHolderMock.kt",
            """
            package com.github.fenrur.vaadindslcodegen

            object VaadinDslApplicationContextHolder {
                fun <T> getBean(clazz: Class<T>): T = throw NotImplementedError()
            }
            """.trimIndent()
        )

        val source = SourceFile.kotlin(
            "SpringComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class SpringComponent : Component()
            """.trimIndent()
        )

        val kotlinCompilation = KotlinCompilation().apply {
            sources = listOf(source, springMock, contextHolderMock) + annotationSources()
            symbolProcessorProviders = mutableListOf(VaadinDslProcessorProvider())
            kspProcessorOptions = mutableMapOf("vaadindsl.mode" to "SPRING")
            inheritClassPath = true
            messageOutputStream = System.out
            workingDir = tempDir
        }
        val result = kotlinCompilation.compile()
        val compilation = CompilationResult(result, kotlinCompilation.kspSourcesDir)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("SpringComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("@Component")
        assertThat(generated).contains("VaadinDslApplicationContextHolder.getBean(SpringComponentFactory::class.java)")
        assertThat(generated).doesNotContain("@ApplicationScoped")
        assertThat(generated).doesNotContain("Arc.container()")
    }

    // ==================== Component Detection Tests ====================

    @Test
    fun `should detect direct Component inheritance`() {
        val source = SourceFile.kotlin(
            "DirectComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class DirectComponent : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("DirectComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("fun HasComponents.directComponent(")
    }

    @Test
    fun `should detect Button inheritance`() {
        val source = SourceFile.kotlin(
            "MySpecialButton.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.button.Button

            @GenDsl
            class MySpecialButton : Button()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("MySpecialButtonFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("fun HasComponents.mySpecialButton(")
    }

    @Test
    fun `should not generate DSL for non-component class`() {
        val source = SourceFile.kotlin(
            "PlainClass.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl

            @GenDsl
            class PlainClass(val value: String)
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("PlainClassFactory.kt")
        assertThat(generated).isNotNull()
        // Should have factory but no DSL function
        assertThat(generated).contains("class PlainClassFactory")
        assertThat(generated).doesNotContain("fun HasComponents.")
    }

    // ==================== Edge Cases ====================

    @Test
    fun `should handle class without constructor params`() {
        val source = SourceFile.kotlin(
            "EmptyComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class EmptyComponent : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("EmptyComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("class EmptyComponentFactory {")
        assertThat(generated).contains("fun create(): EmptyComponent")
    }

    @Test
    fun `should handle nullable types`() {
        val source = SourceFile.kotlin(
            "NullableComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.github.fenrur.vaadindslcodegen.GenDslParam
            import com.vaadin.flow.component.Component

            @GenDsl
            class NullableComponent(
                @GenDslParam val optionalText: String?
            ) : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("NullableComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("optionalText: String?")
    }

    @Test
    fun `should handle generic types`() {
        val source = SourceFile.kotlin(
            "GenericComponent.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.github.fenrur.vaadindslcodegen.GenDslParam
            import com.vaadin.flow.component.Component

            @GenDsl
            class GenericComponent(
                @GenDslParam val items: List<String>
            ) : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val generated = compilation.generatedFile("GenericComponentFactory.kt")
        assertThat(generated).isNotNull()
        assertThat(generated).contains("items: List<String>")
    }

    @Test
    fun `should process multiple GenDsl classes in same file`() {
        val source = SourceFile.kotlin(
            "MultipleComponents.kt",
            """
            package com.example

            import com.github.fenrur.vaadindslcodegen.GenDsl
            import com.vaadin.flow.component.Component

            @GenDsl
            class FirstComponent : Component()

            @GenDsl
            class SecondComponent : Component()
            """.trimIndent()
        )

        val compilation = compile(source)

        assertThat(compilation.result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)

        val first = compilation.generatedFile("FirstComponentFactory.kt")
        val second = compilation.generatedFile("SecondComponentFactory.kt")

        assertThat(first).isNotNull()
        assertThat(second).isNotNull()
        assertThat(first).contains("class FirstComponentFactory")
        assertThat(second).contains("class SecondComponentFactory")
    }
}
