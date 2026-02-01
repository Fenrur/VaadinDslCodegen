package com.github.fenrur.vaadin.codegen

/**
 * Marks a class for DSL factory code generation.
 *
 * When applied to a class that extends a Vaadin Component, the code generator will:
 * 1. Generate a factory class with dependency injection annotations (Quarkus Arc or Spring)
 * 2. Generate a DSL extension function for [com.vaadin.flow.component.HasComponents]
 *
 * Constructor parameters:
 * - Parameters without [GenDslParam] are treated as injected dependencies (via Quarkus Arc or Spring)
 * - Parameters with [GenDslParam] become parameters of the generated DSL function
 *
 * Example:
 * ```kotlin
 * @GenDsl
 * class CustomButton(
 *     private val logger: Logger,           // Injected by DI container
 *     @GenDslParam text: String,            // DSL parameter
 *     @GenDslParam enabled: Boolean = true  // DSL parameter with default
 * ) : Button(text)
 * ```
 *
 * Generated DSL usage:
 * ```kotlin
 * verticalLayout {
 *     customButton("Click me") {
 *         addClickListener { ... }
 *     }
 * }
 * ```
 *
 * @see GenDslParam
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class GenDsl
