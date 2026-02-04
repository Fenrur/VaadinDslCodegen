package com.github.fenrur.vaadin.codegen

/**
 * Marks a property for signal binding extension generation.
 *
 * When applied to a property of type `BindableMutableSignal<T>` or `BindableSignal<T>`,
 * the code generator will create an extension function that allows binding a signal to this property:
 * - `BindableMutableSignal<T>` → generates an extension accepting `MutableSignal<T>`
 * - `BindableSignal<T>` → generates an extension accepting `Signal<T>`
 *
 * **Visibility requirements:**
 * - The property must be `public`
 * - `internal`, `protected` and `private` properties will cause a compilation error
 *
 * Example:
 * ```kotlin
 * class MyComponent : Div() {
 *     @ExposeSignal
 *     val userName: BindableMutableSignal<String> = bindableMutableSignalOf()
 *
 *     @ExposeSignal
 *     val label: BindableSignal<String> = bindableSignalOf()
 * }
 * ```
 *
 * Generated extensions:
 * ```kotlin
 * fun MyComponent.userName(signal: MutableSignal<String>) {
 *     this.userName.bindTo(signal)
 * }
 *
 * fun MyComponent.label(signal: Signal<String>) {
 *     this.label.bindTo(signal)
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
 * @see com.github.fenrur.signal.BindableSignal
 * @see com.github.fenrur.signal.MutableSignal
 * @see com.github.fenrur.signal.Signal
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class ExposeSignal
