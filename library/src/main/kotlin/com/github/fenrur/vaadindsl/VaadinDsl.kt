package com.github.fenrur.vaadindsl

/**
 * Marks a class, type, or function as part of the Vaadin DSL.
 *
 * This annotation is used as a [DslMarker] to prevent implicit receivers
 * from outer scopes being used in nested DSL blocks.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION)
@DslMarker
annotation class VaadinDsl
