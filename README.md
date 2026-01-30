# Vaadin DSL Codegen

A Gradle plugin that generates factory classes and DSL extension functions for Vaadin components.

## Installation

### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
plugins {
    id("com.github.fenrur.vaadin-dsl-codegen") version "1.0.0"
}

dependencies {
    implementation("com.github.fenrur:vaadin-dsl-codegen-library:1.0.0")
}
```

## Configuration

```kotlin
vaadinDslCodegen {
    mode.set(ContainerMode.QUARKUS) // Default: QUARKUS
    // or
    mode.set(ContainerMode.SPRING)
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
    @GenDslParam text: String,            // DSL parameter
    @GenDslParam enabled: Boolean = true  // DSL parameter with default
) : Button(text) {
    init {
        isEnabled = enabled
    }
}
```

#### @GenDslParam

Marks a constructor parameter as a DSL function parameter (not injected).

### Generated Code

For the example above, the plugin generates:

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

## Gradle Task

The plugin registers a `generateVaadinDsl` task that runs before `compileKotlin`.

```bash
./gradlew generateVaadinDsl
```

Generated files are placed in `build/generated-src/vaadin-dsl/`.

## License

MIT License
