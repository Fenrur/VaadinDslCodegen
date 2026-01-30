package com.example.spring

import com.github.fenrur.vaadindslcodegen.GenDsl
import com.github.fenrur.vaadindslcodegen.GenDslParam
import com.vaadin.flow.component.button.Button
import org.slf4j.Logger

/**
 * Example custom button component with Spring injection.
 *
 * The processor will generate:
 * - CustomButtonFactory with @Component annotation
 * - customButton() DSL extension function using VaadinDslApplicationContextHolder
 */
@GenDsl
class CustomButton(
    private val logger: Logger,                    // Spring injected
    @GenDslParam label: String,                    // DSL parameter
    @GenDslParam val primary: Boolean = false      // DSL parameter with default
) : Button(label) {

    init {
        if (primary) {
            addThemeName("primary")
        }
        addClickListener {
            logger.info("CustomButton clicked: $text")
        }
    }
}
