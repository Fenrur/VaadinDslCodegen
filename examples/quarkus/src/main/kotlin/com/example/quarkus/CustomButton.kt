package com.example.quarkus

import com.github.fenrur.vaadindslcodegen.GenDsl
import com.github.fenrur.vaadindslcodegen.GenDslParam
import com.vaadin.flow.component.button.Button
import org.slf4j.Logger

/**
 * Example custom button component with Quarkus Arc injection.
 *
 * The processor will generate:
 * - CustomButtonFactory with @ApplicationScoped and @Unremovable annotations
 * - customButton() DSL extension function for HasComponents
 */
@GenDsl
class CustomButton(
    private val logger: Logger,                    // Quarkus Arc injected
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
