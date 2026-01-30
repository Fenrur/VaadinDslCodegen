package com.github.fenrur.vaadindsl.model

/**
 * Holds metadata about a class annotated with @GenDsl.
 *
 * @property className The simple class name
 * @property packageName The package name
 * @property constructorParams List of constructor parameters
 * @property imports Map of simple type names to fully qualified names
 * @property extendsComponent Whether this class extends a Vaadin Component
 * @property superClass The superclass name (if any)
 */
data class ClassInfo(
    val className: String,
    val packageName: String,
    val constructorParams: List<ConstructorParam>,
    val imports: Map<String, String>,
    val extendsComponent: Boolean,
    val superClass: String? = null
) {
    /**
     * The name of the generated factory class.
     */
    val factoryClassName: String
        get() = "${className}Factory"

    /**
     * The fully qualified name of the generated factory class.
     */
    val factoryQualifiedName: String
        get() = "$packageName.$factoryClassName"

    /**
     * Constructor parameters that are CDI-injected (not annotated with @GenDslParam).
     */
    val injectedParams: List<ConstructorParam>
        get() = constructorParams.filter { !it.isGenDslParam }

    /**
     * Constructor parameters that are DSL function parameters (annotated with @GenDslParam).
     */
    val dslParams: List<ConstructorParam>
        get() = constructorParams.filter { it.isGenDslParam }

    /**
     * The DSL function name (lowercase first letter of class name).
     */
    val dslFunctionName: String
        get() = className.replaceFirstChar { it.lowercase() }
}
