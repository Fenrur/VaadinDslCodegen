package com.github.fenrur.vaadin.codegen

/**
 * Marks a constructor parameter as a DSL function parameter.
 *
 * When used in a class annotated with [GenDsl], this annotation indicates that
 * the parameter should be passed to the generated DSL function rather than
 * being injected by the DI container (Quarkus Arc or Spring).
 *
 * Example:
 * ```kotlin
 * @GenDsl
 * class CustomButton(
 *     private val logger: Logger,           // Injected by DI container (no @GenDslParam)
 *     @GenDslParam text: String,            // DSL parameter
 *     @GenDslParam enabled: Boolean = true  // DSL parameter with default value
 * ) : Button(text)
 * ```
 *
 * The generated factory will have:
 * - Constructor receiving `logger` (injected by DI container)
 * - `create(text: String, enabled: Boolean = true)` method
 *
 * The generated DSL function will have:
 * - `text: String` parameter
 * - `enabled: Boolean = true` parameter
 * - `block: CustomButton.() -> Unit = {}` lambda parameter
 *
 * @see GenDsl
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class GenDslParam
