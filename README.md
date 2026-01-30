# Vaadin DSL Codegen

A KSP (Kotlin Symbol Processing) processor that generates factory classes and DSL extension functions for Vaadin components.

## Installation

### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
plugins {
    kotlin("jvm") version "2.1.0"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.fenrur.vaadin-dsl-codegen:library:1.0.0")
    ksp("com.github.fenrur.vaadin-dsl-codegen:processor:1.0.0")
}
```

## Configuration

Configure the processor mode via KSP arguments:

```kotlin
ksp {
    arg("vaadindsl.mode", "QUARKUS") // or "SPRING"
}
```

### Modes

| Mode | Container | Bean retrieval |
|------|-----------|----------------|
| `QUARKUS` | Arc | `Arc.container().instance(...).get()` |
| `SPRING` | Spring | `VaadinDslApplicationContextHolder.getBean(...)` |

## Usage

### Annotations

#### @GenDsl

Marks a class for DSL factory generation.

```kotlin
import com.github.fenrur.vaadindslcodegen.GenDsl
import com.github.fenrur.vaadindslcodegen.GenDslParam

@GenDsl
class CustomButton(
    private val logger: Logger,           // CDI/Spring injected
    @GenDslParam val text: String,        // DSL parameter
    @GenDslParam val enabled: Boolean = true  // DSL parameter with default
) : Button(text) {
    init {
        isEnabled = enabled
    }
}
```

#### @GenDslParam

Marks a constructor parameter as a DSL function parameter (not injected).

### Generated Code

For the example above, the processor generates:

**Quarkus mode:**
```kotlin
@ApplicationScoped
@Unremovable
class CustomButtonFactory(
    private val logger: Logger
) {
    fun create(text: String, enabled: Boolean = true): CustomButton {
        return CustomButton(logger, text, enabled)
    }
}

@VaadinDsl
fun HasComponents.customButton(
    text: String,
    enabled: Boolean = true,
    block: CustomButton.() -> Unit = {}
): CustomButton {
    val factory = Arc.container().instance(CustomButtonFactory::class.java).get()
    val component = factory.create(text, enabled)
    add(component)
    component.block()
    return component
}
```

**Spring mode:**
```kotlin
@Component
class CustomButtonFactory(
    private val logger: Logger
) {
    fun create(text: String, enabled: Boolean = true): CustomButton {
        return CustomButton(logger, text, enabled)
    }
}

@VaadinDsl
fun HasComponents.customButton(
    text: String,
    enabled: Boolean = true,
    block: CustomButton.() -> Unit = {}
): CustomButton {
    val factory = VaadinDslApplicationContextHolder.getBean(CustomButtonFactory::class.java)
    val component = factory.create(text, enabled)
    add(component)
    component.block()
    return component
}
```

### DSL Usage

```kotlin
verticalLayout {
    customButton("Click me") {
        addClickListener {
            // handle click
        }
    }

    customButton("Disabled", enabled = false)
}
```

## How It Works

The processor uses KSP to:

1. Find all classes annotated with `@GenDsl`
2. Analyze the primary constructor parameters
3. Detect `@GenDslParam` annotations to separate DSL params from injected dependencies
4. Check if the class extends a Vaadin `Component` (via type resolution)
5. Generate factory classes with proper DI annotations
6. Generate DSL extension functions for `HasComponents`

### Advantages of KSP

- **Type-safe**: Full type resolution for accurate Component detection
- **Fast**: 2x faster than KAPT
- **Incremental**: Only reprocesses changed files
- **Kotlin-native**: Direct access to Kotlin symbols and annotations

## Generated Files Location

Generated files are placed in `build/generated/ksp/main/kotlin/`.

## Spring Mode Setup

When using Spring mode, make sure `VaadinDslApplicationContextHolder` is available:

```kotlin
// This class is provided in the library module
// Just make sure Spring component scanning includes the package
@ComponentScan("com.github.fenrur.vaadindslcodegen")
```

## License

MIT License
