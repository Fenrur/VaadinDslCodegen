package com.github.fenrur.vaadindsl

import org.gradle.api.provider.Property

/**
 * Configuration extension for the Vaadin DSL Codegen plugin.
 */
abstract class VaadinDslCodegenExtension {

    /**
     * The container mode to use for dependency injection.
     * - QUARKUS: Uses Arc container (io.quarkus.arc.Arc)
     * - SPRING: Uses VaadinDslApplicationContextHolder
     *
     * Default: QUARKUS
     */
    abstract val mode: Property<ContainerMode>

    init {
        mode.convention(ContainerMode.QUARKUS)
    }
}

/**
 * The dependency injection container mode.
 */
enum class ContainerMode {
    /**
     * Quarkus mode using Arc container.
     * Generated code uses: Arc.container().instance(FactoryClass::class.java).get()
     */
    QUARKUS,

    /**
     * Spring mode using VaadinDslApplicationContextHolder.
     * Generated code uses: VaadinDslApplicationContextHolder.getBean(FactoryClass::class.java)
     */
    SPRING
}
