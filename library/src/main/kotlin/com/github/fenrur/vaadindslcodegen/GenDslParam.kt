package com.github.fenrur.vaadindslcodegen

/**
 * Marks a constructor parameter as a DSL function parameter.
 *
 * When used in a class annotated with [GenDsl], this annotation indicates that
 * the parameter should be passed to the generated DSL function rather than
 * being injected via CDI.
 *
 * Example:
 * ```kotlin
 * @GenDsl
 * class CustomButton(
 *     private val logger: Logger,           // CDI-injected (no @GenDslParam)
 *     @GenDslParam text: String,            // DSL parameter
 *     @GenDslParam enabled: Boolean = true  // DSL parameter with default value
 * ) : Button(text)
 * ```
 *
 * The generated factory will have:
 * - Constructor receiving `logger` (CDI-injected)
 * - `create(text: String, enabled: Boolean = true)` method
 *
 * The generated DSL function will have:
 * - `text: String` parameter
 * - `enabled: Boolean = true` parameter
 * - `block: CustomButton.() -> Unit = {}` lambda parameter
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class GenDslParam
