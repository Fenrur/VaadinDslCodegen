package com.example.quarkus

import com.github.fenrur.vaadindslcodegen.GenDsl
import com.github.fenrur.vaadindslcodegen.GenDslParam
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Paragraph

/**
 * Example card component without any injected dependencies.
 *
 * The processor will generate:
 * - InfoCardFactory with @ApplicationScoped and @Unremovable annotations (no constructor params)
 * - infoCard() DSL extension function for HasComponents
 */
@GenDsl
class InfoCard(
    @GenDslParam val title: String,
    @GenDslParam val description: String
) : Div() {

    init {
        addClassName("info-card")
        add(H3(title))
        add(Paragraph(description))
    }
}
