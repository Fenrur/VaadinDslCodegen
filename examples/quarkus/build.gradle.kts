import com.github.fenrur.vaadin.codegen.VaadinDslCodegenExtension.Mode

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.1.0"
    id("io.quarkus") version "3.17.7"
    id("com.vaadin") version "24.6.3"
    id("com.github.fenrur.vaadin-codegen") // Applies KSP automatically
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
    maven { url = uri("https://jitpack.io") }
}

val vaadinVersion = "24.6.3"
val quarkusVersion = "3.17.7"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    // BOM
    implementation(enforcedPlatform("com.vaadin:vaadin-bom:$vaadinVersion"))
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:$quarkusVersion"))

    // Vaadin Quarkus
    implementation("com.vaadin:vaadin-quarkus-extension:$vaadinVersion")
    implementation("com.vaadin:vaadin")

    // Our library
    implementation(project(":library"))
    ksp(project(":processor"))

    // Signal library for @ExposeSignal
    implementation("com.github.fenrur:signal:v1.0.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Test
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

vaadinDslCodegen {
    mode = Mode.QUARKUS
}

allOpen {
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.ws.rs.Path")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

vaadin {
    pnpmEnable = true
}

// Fix circular dependency between KSP and Quarkus
afterEvaluate {
    tasks.named("kspKotlin") {
        setDependsOn(dependsOn.filterNot {
            it.toString().contains("quarkusGenerateCode")
        })
    }
}
