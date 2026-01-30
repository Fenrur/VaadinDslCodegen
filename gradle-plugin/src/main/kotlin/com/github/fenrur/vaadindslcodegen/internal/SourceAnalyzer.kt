package com.github.fenrur.vaadindslcodegen.internal

import com.github.fenrur.vaadindslcodegen.model.ClassInfo
import com.github.fenrur.vaadindslcodegen.model.ConstructorParam
import java.io.File

/**
 * Analyzes Kotlin source files to find classes annotated with @GenDsl.
 */
object SourceAnalyzer {

    private val PACKAGE_REGEX = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
    private val IMPORT_REGEX = Regex("""^\s*import\s+([\w.]+)(?:\s+as\s+(\w+))?""", RegexOption.MULTILINE)
    private val GEN_DSL_CLASS_REGEX = Regex(
        """@GenDsl\s+(?:@\w+(?:\([^)]*\))?\s+)*(?:public\s+|private\s+|internal\s+|protected\s+)?(?:open\s+|abstract\s+|sealed\s+|data\s+)?class\s+(\w+)(?:<[^>]+>)?\s*(?:\(\s*([\s\S]*?)\s*\))?\s*(?::\s*([^{]+))?""",
        RegexOption.MULTILINE
    )
    private val PARAM_REGEX = Regex(
        """(@GenDslParam\s+)?(?:private\s+|protected\s+)?(?:val\s+|var\s+)?(\w+)\s*:\s*([^,=]+)(?:\s*=\s*([^,]+))?"""
    )

    private val VAADIN_COMPONENT_TYPES = setOf(
        "Component", "Composite", "Div", "Span", "Button", "TextField", "PasswordField",
        "TextArea", "Checkbox", "ComboBox", "Select", "DatePicker", "TimePicker",
        "Upload", "Image", "Anchor", "RouterLink", "Grid", "TreeGrid", "FormLayout",
        "VerticalLayout", "HorizontalLayout", "FlexLayout", "SplitLayout", "Tabs",
        "Dialog", "Notification", "ProgressBar", "Icon", "Avatar", "Badge",
        "Details", "Accordion", "MenuBar", "ContextMenu", "ListBox", "RadioButtonGroup",
        "CheckboxGroup", "MultiSelectComboBox", "RichTextEditor", "Board", "Chart"
    )

    /**
     * Finds all classes annotated with @GenDsl in the given file.
     */
    fun findGenDslClasses(file: File): List<ClassInfo> {
        val content = file.readText()

        // Check if file contains @GenDsl
        if (!content.contains("@GenDsl")) {
            return emptyList()
        }

        val packageName = PACKAGE_REGEX.find(content)?.groupValues?.get(1) ?: ""
        val imports = parseImports(content)

        return GEN_DSL_CLASS_REGEX.findAll(content).map { match ->
            val className = match.groupValues[1]
            val constructorContent = match.groupValues[2]
            val inheritance = match.groupValues[3].trim()

            val constructorParams = parseConstructorParams(constructorContent, imports)
            val (extendsComponent, superClass) = analyzeInheritance(inheritance, imports)

            ClassInfo(
                className = className,
                packageName = packageName,
                constructorParams = constructorParams,
                imports = imports,
                extendsComponent = extendsComponent,
                superClass = superClass
            )
        }.toList()
    }

    private fun parseImports(content: String): Map<String, String> {
        val imports = mutableMapOf<String, String>()

        IMPORT_REGEX.findAll(content).forEach { match ->
            val fullName = match.groupValues[1]
            val alias = match.groupValues[2].ifEmpty { fullName.substringAfterLast('.') }
            imports[alias] = fullName
        }

        return imports
    }

    private fun parseConstructorParams(constructorContent: String, imports: Map<String, String>): List<ConstructorParam> {
        if (constructorContent.isBlank()) {
            return emptyList()
        }

        val params = mutableListOf<ConstructorParam>()
        val paramStrings = splitParams(constructorContent)

        for (paramStr in paramStrings) {
            val match = PARAM_REGEX.find(paramStr.trim()) ?: continue

            val isGenDslParam = match.groupValues[1].isNotBlank()
            val name = match.groupValues[2]
            val rawType = match.groupValues[3].trim()
            val defaultValue = match.groupValues[4].trim().ifEmpty { null }

            val type = resolveType(rawType, imports)

            params.add(
                ConstructorParam(
                    name = name,
                    type = type,
                    isGenDslParam = isGenDslParam,
                    hasDefault = defaultValue != null,
                    defaultValue = defaultValue
                )
            )
        }

        return params
    }

    private fun splitParams(content: String): List<String> {
        val params = mutableListOf<String>()
        var depth = 0
        var current = StringBuilder()

        for (char in content) {
            when (char) {
                '<', '(', '[', '{' -> {
                    depth++
                    current.append(char)
                }
                '>', ')', ']', '}' -> {
                    depth--
                    current.append(char)
                }
                ',' -> {
                    if (depth == 0) {
                        params.add(current.toString())
                        current = StringBuilder()
                    } else {
                        current.append(char)
                    }
                }
                else -> current.append(char)
            }
        }

        if (current.isNotBlank()) {
            params.add(current.toString())
        }

        return params
    }

    private fun analyzeInheritance(inheritance: String, imports: Map<String, String>): Pair<Boolean, String?> {
        if (inheritance.isBlank()) {
            return false to null
        }

        // Get the first supertype (before any comma for interfaces)
        val superType = inheritance.split(',').first().trim()
        val superClassName = superType.substringBefore('(').substringBefore('<').trim()

        val extendsComponent = VAADIN_COMPONENT_TYPES.contains(superClassName) ||
                imports[superClassName]?.contains("vaadin") == true ||
                imports[superClassName]?.contains("Component") == true

        return extendsComponent to superClassName
    }

    private fun resolveType(rawType: String, imports: Map<String, String>): String {
        val baseType = rawType.substringBefore('<').substringBefore('?').trim()

        // Check if it's a built-in Kotlin type
        val kotlinBuiltins = setOf(
            "Boolean", "Byte", "Short", "Int", "Long", "Float", "Double", "Char", "String",
            "Unit", "Any", "Nothing", "List", "Set", "Map", "MutableList", "MutableSet", "MutableMap"
        )

        if (baseType in kotlinBuiltins) {
            return rawType.trim()
        }

        // Check imports
        val imported = imports[baseType]
        if (imported != null) {
            return rawType.replace(baseType, imported)
        }

        return rawType.trim()
    }
}
