package com.example.quarkus

import com.github.fenrur.signal.BindableMutableSignal
import com.github.fenrur.signal.bindableMutableSignalOf
import com.github.fenrur.vaadin.codegen.ExposeSignal
import com.github.fenrur.vaadin.codegen.GenDsl
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.H3
import com.vaadin.flow.component.html.Paragraph

/**
 * Example component demonstrating @ExposeSignal annotation.
 *
 * The processor will generate:
 * - SignalCardFactory with @ApplicationScoped (from @GenDsl)
 * - signalCard() DSL extension function
 * - title(signal) extension function (from @ExposeSignal on title property)
 * - content(signal) extension function (from @ExposeSignal on content property)
 */
@GenDsl
class SignalCard : Div() {

    @ExposeSignal
    val title: BindableMutableSignal<String> = bindableMutableSignalOf("")

    @ExposeSignal
    val content: BindableMutableSignal<String> = bindableMutableSignalOf("")

    private val titleElement = H3()
    private val contentElement = Paragraph()

    init {
        addClassName("signal-card")
        add(titleElement)
        add(contentElement)
    }
}
