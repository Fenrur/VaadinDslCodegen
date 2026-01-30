package com.github.fenrur.vaadindslcodegen

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle plugin that configures Vaadin DSL code generation.
 *
 * This plugin:
 * 1. Applies the KSP plugin automatically
 * 2. Creates the `vaadinDslCodegen` extension for configuration
 * 3. Configures KSP arguments based on the extension settings
 *
 * Example usage:
 * ```kotlin
 * plugins {
 *     id("com.github.fenrur.vaadin-dsl-codegen") version "1.0.0"
 * }
 *
 * vaadinDslCodegen {
 *     mode = Mode.QUARKUS
 * }
 * ```
 */
class VaadinDslCodegenPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // Apply KSP plugin automatically
        project.pluginManager.apply("com.google.devtools.ksp")

        // Create the extension
        val extension = project.extensions.create(
            "vaadinDslCodegen",
            VaadinDslCodegenExtension::class.java
        )

        // Set default value
        extension.mode.convention(VaadinDslCodegenExtension.Mode.QUARKUS)

        // Configure KSP after project evaluation
        project.afterEvaluate {
            project.extensions.findByType(KspExtension::class.java)?.let { ksp ->
                ksp.arg("vaadindsl.mode", extension.mode.get().name)
            }
        }
    }
}
