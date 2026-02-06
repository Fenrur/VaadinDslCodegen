import com.github.fenrur.vaadin.codegen.VaadinDslCodegenExtension.Mode

plugins {
    kotlin("jvm")
    kotlin("plugin.allopen")
    id("io.quarkus")
    id("com.vaadin")
    id("com.github.fenrur.vaadin-codegen")
}

val vaadinVersion = "24.6.3"
val quarkusVersion = "3.17.7"
val vaadinCodegenVersion = "1.0.0"

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

    // Vaadin DSL Codegen library
    implementation("com.github.fenrur.vaadin-codegen:library:$vaadinCodegenVersion")
    ksp("com.github.fenrur.vaadin-codegen:processor:$vaadinCodegenVersion")

    // Signal library for @ExposeSignal
    implementation("com.github.fenrur:signal:1.0.0")

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
