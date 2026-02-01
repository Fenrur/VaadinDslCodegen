# Vaadin Codegen

A KSP (Kotlin Symbol Processing) processor that generates factory classes and DSL extension functions for Vaadin components.

## Installation

[![](https://jitpack.io/v/fenrur/vaadin-codegen.svg)](https://jitpack.io/#fenrur/vaadin-codegen)

### Gradle (Kotlin DSL)

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "com.github.fenrur.vaadin-codegen") {
                useModule("com.github.fenrur.vaadin-codegen:gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

// build.gradle.kts
import com.github.fenrur.vaadin.codegen.VaadinDslCodegenExtension.Mode

plugins {
    kotlin("jvm") version "2.1.0"
    id("com.github.fenrur.vaadin-codegen") version "1.0.0" // Applies KSP automatically
}

dependencies {
    implementation("com.github.fenrur.vaadin-codegen:library:1.0.0")
    ksp("com.github.fenrur.vaadin-codegen:processor:1.0.0")
}

vaadinDslCodegen {
    mode = Mode.QUARKUS // or Mode.SPRING
}
```

### Gradle (Groovy DSL)

```groovy
// settings.gradle
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url 'https://jitpack.io' }
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == 'com.github.fenrur.vaadin-codegen') {
                useModule("com.github.fenrur.vaadin-codegen:gradle-plugin:${requested.version}")
            }
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

// build.gradle
import com.github.fenrur.vaadin.codegen.VaadinDslCodegenExtension.Mode

plugins {
    id 'org.jetbrains.kotlin.jvm' version '2.1.0'
    id 'com.github.fenrur.vaadin-codegen' version '1.0.0' // Applies KSP automatically
}

dependencies {
    implementation 'com.github.fenrur.vaadin-codegen:library:1.0.0'
    ksp 'com.github.fenrur.vaadin-codegen:processor:1.0.0'
}

vaadinDslCodegen {
    mode = Mode.QUARKUS // or Mode.SPRING
}
```

## Configuration

### Modes

| Mode | Container | Bean retrieval |
|------|-----------|----------------|
| `QUARKUS` | Arc | `Arc.container().instance(...).get()` |
| `SPRING` | Spring | `VaadinDslApplicationContextHolder.getBean(...)` |

## Spring Mode Setup

When using Spring mode, make sure `VaadinDslApplicationContextHolder` is available:

```kotlin
// This class is provided in the library module
// Just make sure Spring component scanning includes the package
@ComponentScan("com.github.fenrur.vaadin.codegen")
```

## Quarkus + KSP Circular Dependency Fix

When using KSP with Quarkus, you may encounter a circular dependency error between `kspKotlin`, `quarkusGenerateCode`, and `processResources` tasks.

See [quarkusio/quarkus#29698](https://github.com/quarkusio/quarkus/issues/29698) for more details.

### Fix for Gradle (Kotlin DSL)

Add this to your `build.gradle.kts`:

```kotlin
// Fix circular dependency between KSP and Quarkus
afterEvaluate {
    tasks.named("kspKotlin") {
        setDependsOn(dependsOn.filterNot {
            it.toString().contains("quarkusGenerateCode")
        })
    }
}
```

### Fix for Gradle (Groovy DSL)

Add this to your `build.gradle`:

```groovy
// Fix circular dependency between KSP and Quarkus (https://github.com/quarkusio/quarkus/issues/29698)
afterEvaluate {
    tasks.getByName("quarkusGenerateCode").setDependsOn(
        tasks.getByName("quarkusGenerateCode").dependsOn.findAll { dep ->
            !(dep instanceof Provider && ((Provider) dep).get().name == "processResources")
        }
    )
    tasks.getByName("quarkusGenerateCodeDev").setDependsOn(
        tasks.getByName("quarkusGenerateCodeDev").dependsOn.findAll { dep ->
            !(dep instanceof Provider && ((Provider) dep).get().name == "processResources")
        }
    )
}
```

## Push Configuration (WebSocket)

For reactive features and server-side push updates, configure your `AppShellConfigurator` with WebSocket transport.

### Quarkus

Create a separate class implementing `AppShellConfigurator`:

```kotlin
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
@Push(PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
@PWA(name = "My Application", shortName = "MyApp")
@Theme("lumo")
class VaadinAppShellConfigurator : AppShellConfigurator
```

### Spring Boot

Add `AppShellConfigurator` to your main application class:

```kotlin
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.server.PWA
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.example.spring", "com.github.fenrur.vaadin.codegen"])
@Push(PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
@PWA(name = "My Application", shortName = "MyApp")
@Theme("lumo")
class Application : AppShellConfigurator

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
```

### Push Modes

| Mode | Description |
|------|-------------|
| `PushMode.AUTOMATIC` | Server pushes changes automatically when UI is modified |
| `PushMode.MANUAL` | You must call `ui.push()` manually to send updates |
| `PushMode.DISABLED` | Push is disabled |

### Transport Options

| Transport | Description |
|-----------|-------------|
| `Transport.WEBSOCKET` | Pure WebSocket connection (recommended for real-time updates) |
| `Transport.WEBSOCKET_XHR` | WebSocket with XHR fallback (default) |
| `Transport.LONG_POLLING` | HTTP long polling (for environments where WebSocket is not available) |

## Usage

### Annotations

#### @GenDsl

Marks a class for DSL factory generation.

```kotlin
import com.github.fenrur.vaadin.codegen.GenDsl
import com.github.fenrur.vaadin.codegen.GenDslParam

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

## Generated Files Location

Generated files are placed in `build/generated/ksp/main/kotlin/`.

## Examples

The repository includes working examples for both Quarkus and Spring:

```
examples/
├── quarkus/     # Quarkus + Vaadin example
└── spring/      # Spring Boot + Vaadin example
```

### Running the Examples

**Quarkus:**
```bash
cd examples/quarkus
../gradlew quarkusDev
# Open http://localhost:8082
```

**Spring:**
```bash
cd examples/spring
../gradlew bootRun
# Open http://localhost:8081
```

### Example Components

Both examples include:

- **CustomButton**: A button component with DI-injected Logger and DSL parameters
- **InfoCard**: A simple card component with only DSL parameters
- **MainView**: Demonstrates using the generated DSL functions

```kotlin
// Example component definition
@GenDsl
class CustomButton(
    private val logger: Logger,           // Injected by DI container
    @GenDslParam label: String,           // DSL parameter
    @GenDslParam val primary: Boolean     // DSL parameter
) : Button(label)

// Usage in a view
class MainView : VerticalLayout() {
    init {
        customButton("Click Me", primary = true) {
            addClickListener { /* ... */ }
        }
        infoCard("Title", "Description")
    }
}
```

## License

MIT License
