package com.github.fenrur.vaadin.codegen

/**
 * Marks a property for signal binding extension generation.
 *
 * When applied to a property of type `BindableMutableSignal<T>`, the code generator will
 * create an extension function that allows binding a `MutableSignal<T>` to this property.
 *
 * **Visibility requirements:**
 * - The property must be `public` or `internal`
 * - `protected` and `private` properties will cause a compilation error
 *
 * Example:
 * ```kotlin
 * class MyComponent : Div() {
 *     @ExposeSignal
 *     internal val userName: BindableMutableSignal<String> = bindableMutableSignalOf()
 * }
 * ```
 *
 * Generated extension:
 * ```kotlin
 * fun MyComponent.userName(signal: MutableSignal<String>) {
 *     this.userName.bindTo(signal)
 * }
 * ```
 *
 * Usage:
 * ```kotlin
 * val nameSignal = mutableSignalOf("John")
 * myComponent.userName(nameSignal)
 * ```
 *
 * @see com.github.fenrur.signal.BindableMutableSignal
 * @see com.github.fenrur.signal.MutableSignal
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ExposeSignal
