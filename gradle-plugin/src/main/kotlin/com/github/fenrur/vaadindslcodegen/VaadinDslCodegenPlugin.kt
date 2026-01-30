package com.github.fenrur.vaadindslcodegen

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

/**
 * Gradle plugin for generating Vaadin DSL factory classes.
 *
 * Usage in build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("com.github.fenrur.vaadin-dsl-codegen")
 * }
 *
 * vaadinDslCodegen {
 *     mode.set(ContainerMode.QUARKUS) // or ContainerMode.SPRING
 * }
 * ```
 */
class VaadinDslCodegenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Create extension
        val extension = project.extensions.create(
            "vaadinDslCodegen",
            VaadinDslCodegenExtension::class.java
        )

        // Register task
        val generateTask = project.tasks.register(
            "generateVaadinDsl",
            VaadinDslCodegenTask::class.java
        ) { task ->
            task.mode.set(extension.mode)
            task.outputDir.set(project.layout.buildDirectory.dir("generated-src/vaadin-dsl"))

            // Configure source files after project evaluation
            project.afterEvaluate {
                val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
                val mainSourceSet = sourceSets?.findByName("main")

                if (mainSourceSet != null) {
                    task.sourceFiles = mainSourceSet.allSource.matching {
                        it.include("**/*.kt")
                    }
                } else {
                    // Fallback to standard Kotlin source directory
                    task.sourceFiles = project.files("src/main/kotlin")
                }
            }
        }

        // Add generated sources to source set
        project.afterEvaluate {
            val sourceSets = project.extensions.findByType(SourceSetContainer::class.java)
            val mainSourceSet = sourceSets?.findByName("main")

            mainSourceSet?.java?.srcDir(
                project.layout.buildDirectory.dir("generated-src/vaadin-dsl")
            )

            // Make compileKotlin depend on generate task
            project.tasks.findByName("compileKotlin")?.dependsOn(generateTask)
        }
    }
}
