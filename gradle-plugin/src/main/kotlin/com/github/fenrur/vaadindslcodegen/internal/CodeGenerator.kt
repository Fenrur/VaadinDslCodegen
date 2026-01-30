package com.github.fenrur.vaadindslcodegen.internal

import com.github.fenrur.vaadindslcodegen.ContainerMode
import com.github.fenrur.vaadindslcodegen.model.ClassInfo
import java.io.File

/**
 * Generates factory classes and DSL extension functions.
 */
object CodeGenerator {

    /**
     * Generates the factory class and DSL function for the given class info.
     */
    fun generate(classInfo: ClassInfo, mode: ContainerMode, outputDir: File) {
        val packageDir = File(outputDir, classInfo.packageName.replace('.', '/'))
        packageDir.mkdirs()

        val outputFile = File(packageDir, "${classInfo.factoryClassName}.kt")
        val code = generateCode(classInfo, mode)
        outputFile.writeText(code)
    }

    private fun generateCode(classInfo: ClassInfo, mode: ContainerMode): String {
        val sb = StringBuilder()

        // Package declaration
        sb.appendLine("package ${classInfo.packageName}")
        sb.appendLine()

        // Imports
        sb.appendLine("import com.github.fenrur.vaadindslcodegen.VaadinDsl")

        when (mode) {
            ContainerMode.QUARKUS -> {
                sb.appendLine("import io.quarkus.arc.Arc")
                sb.appendLine("import io.quarkus.arc.Unremovable")
                sb.appendLine("import jakarta.enterprise.context.ApplicationScoped")
            }
            ContainerMode.SPRING -> {
                sb.appendLine("import com.github.fenrur.vaadindslcodegen.VaadinDslApplicationContextHolder")
                sb.appendLine("import org.springframework.stereotype.Component")
            }
        }

        if (classInfo.extendsComponent) {
            sb.appendLine("import com.vaadin.flow.component.HasComponents")
        }

        // Collect unique imports from constructor params
        val paramImports = mutableSetOf<String>()
        classInfo.constructorParams.forEach { param ->
            val baseType = param.type.substringBefore('<').substringBefore('?')
            if (baseType.contains('.')) {
                paramImports.add(baseType)
            }
        }
        paramImports.forEach { sb.appendLine("import $it") }

        sb.appendLine()

        // Factory class
        generateFactoryClass(sb, classInfo, mode)

        // DSL function (only for Vaadin components)
        if (classInfo.extendsComponent) {
            sb.appendLine()
            generateDslFunction(sb, classInfo, mode)
        }

        return sb.toString()
    }

    private fun generateFactoryClass(sb: StringBuilder, classInfo: ClassInfo, mode: ContainerMode) {
        // Annotations
        when (mode) {
            ContainerMode.QUARKUS -> {
                sb.appendLine("@ApplicationScoped")
                sb.appendLine("@Unremovable")
            }
            ContainerMode.SPRING -> {
                sb.appendLine("@Component")
            }
        }

        // Class declaration with injected params
        val injectedParams = classInfo.injectedParams
        if (injectedParams.isEmpty()) {
            sb.appendLine("class ${classInfo.factoryClassName} {")
        } else {
            sb.appendLine("class ${classInfo.factoryClassName}(")
            injectedParams.forEachIndexed { index, param ->
                val comma = if (index < injectedParams.size - 1) "," else ""
                sb.appendLine("    private val ${param.name}: ${getSimpleType(param.type)}$comma")
            }
            sb.appendLine(") {")
        }

        sb.appendLine()

        // create() method
        val dslParams = classInfo.dslParams
        if (dslParams.isEmpty()) {
            sb.appendLine("    fun create(): ${classInfo.className} {")
        } else {
            sb.appendLine("    fun create(")
            dslParams.forEachIndexed { index, param ->
                val comma = if (index < dslParams.size - 1) "," else ""
                val default = if (param.hasDefault) " = ${param.defaultValue}" else ""
                sb.appendLine("        ${param.name}: ${getSimpleType(param.type)}$default$comma")
            }
            sb.appendLine("    ): ${classInfo.className} {")
        }

        // Constructor call
        val allParamNames = classInfo.constructorParams.map { it.name }
        sb.appendLine("        return ${classInfo.className}(${allParamNames.joinToString(", ")})")
        sb.appendLine("    }")

        sb.appendLine("}")
    }

    private fun generateDslFunction(sb: StringBuilder, classInfo: ClassInfo, mode: ContainerMode) {
        val dslParams = classInfo.dslParams

        // Function signature
        sb.appendLine("@VaadinDsl")
        if (dslParams.isEmpty()) {
            sb.appendLine("fun HasComponents.${classInfo.dslFunctionName}(")
            sb.appendLine("    block: ${classInfo.className}.() -> Unit = {}")
            sb.appendLine("): ${classInfo.className} {")
        } else {
            sb.appendLine("fun HasComponents.${classInfo.dslFunctionName}(")
            dslParams.forEachIndexed { index, param ->
                val default = if (param.hasDefault) " = ${param.defaultValue}" else ""
                sb.appendLine("    ${param.name}: ${getSimpleType(param.type)}$default,")
            }
            sb.appendLine("    block: ${classInfo.className}.() -> Unit = {}")
            sb.appendLine("): ${classInfo.className} {")
        }

        // Get factory from container
        when (mode) {
            ContainerMode.QUARKUS -> {
                sb.appendLine("    val factory = Arc.container().instance(${classInfo.factoryClassName}::class.java).get()")
            }
            ContainerMode.SPRING -> {
                sb.appendLine("    val factory = VaadinDslApplicationContextHolder.getBean(${classInfo.factoryClassName}::class.java)")
            }
        }

        // Create component
        val dslParamNames = dslParams.map { it.name }
        if (dslParamNames.isEmpty()) {
            sb.appendLine("    val component = factory.create()")
        } else {
            sb.appendLine("    val component = factory.create(${dslParamNames.joinToString(", ")})")
        }

        // Add to parent and apply block
        sb.appendLine("    add(component)")
        sb.appendLine("    component.block()")
        sb.appendLine("    return component")
        sb.appendLine("}")
    }

    private fun getSimpleType(type: String): String {
        // Extract simple type name from fully qualified name
        return type.replace(Regex("""(\w+\.)+(\w+)""")) { match ->
            match.groupValues[2]
        }
    }
}
