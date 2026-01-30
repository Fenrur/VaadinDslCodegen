package com.example.spring

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.Route

/**
 * Main view demonstrating the generated DSL components.
 */
@Route("")
class MainView : VerticalLayout() {

    init {
        // Use the generated DSL functions
        customButton("Click Me", primary = true) {
            addClickListener {
                // Add a new InfoCard when clicked
                infoCard("Clicked!", "Button was clicked at ${System.currentTimeMillis()}")
            }
        }

        customButton("Secondary Button", primary = false) {
            addClickListener {
                infoCard("Secondary", "Secondary button clicked")
            }
        }

        infoCard("Welcome", "This is a demo of the Vaadin DSL Codegen for Spring")
    }
}
