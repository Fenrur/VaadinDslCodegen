package com.github.fenrur.vaadindsl

import com.github.fenrur.vaadindsl.internal.CodeGenerator
import com.github.fenrur.vaadindsl.internal.SourceAnalyzer
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Gradle task that generates factory classes and DSL functions
 * for classes annotated with @GenDsl.
 */
@CacheableTask
abstract class VaadinDslCodegenTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract var sourceFiles: FileCollection

    @get:Input
    abstract val mode: Property<ContainerMode>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    init {
        group = "vaadin-dsl"
        description = "Generates factory classes and DSL functions for @GenDsl annotated classes"
        mode.convention(ContainerMode.QUARKUS)
    }

    @TaskAction
    fun generate() {
        val outputDirectory = outputDir.get().asFile
        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()

        val containerMode = mode.get()

        logger.info("Vaadin DSL Codegen: Using $containerMode mode")

        var generatedCount = 0

        sourceFiles.forEach { file ->
            if (file.extension == "kt") {
                try {
                    val classes = SourceAnalyzer.findGenDslClasses(file)
                    classes.forEach { classInfo ->
                        logger.info("Generating factory for ${classInfo.className}")
                        CodeGenerator.generate(classInfo, containerMode, outputDirectory)
                        generatedCount++
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to process ${file.path}: ${e.message}")
                }
            }
        }

        logger.lifecycle("Vaadin DSL Codegen: Generated $generatedCount factory classes")
    }
}
