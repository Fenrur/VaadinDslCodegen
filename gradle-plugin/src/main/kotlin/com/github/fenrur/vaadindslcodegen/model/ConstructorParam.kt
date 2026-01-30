package com.github.fenrur.vaadindslcodegen.model

/**
 * Represents a constructor parameter of a class annotated with @GenDsl.
 *
 * @property name The parameter name
 * @property type The fully qualified type name
 * @property isGenDslParam Whether this parameter is annotated with @GenDslParam
 * @property hasDefault Whether this parameter has a default value
 * @property defaultValue The default value expression (if any)
 */
data class ConstructorParam(
    val name: String,
    val type: String,
    val isGenDslParam: Boolean,
    val hasDefault: Boolean,
    val defaultValue: String? = null
)
