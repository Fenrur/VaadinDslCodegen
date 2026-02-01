package com.example.quarkus

import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
@Push(PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
@PWA(name = "Vaadin Codegen Quarkus Example", shortName = "Codegen Quarkus")
@Theme("lumo")
class VaadinAppShellConfigurator : AppShellConfigurator
