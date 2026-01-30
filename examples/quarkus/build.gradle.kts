plugins {
    kotlin("jvm")
    kotlin("plugin.allopen") version "2.1.0"
    id("io.quarkus") version "3.17.7"
    id("com.vaadin") version "24.6.3"
    id("com.google.devtools.ksp") version "2.1.0-1.0.29"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
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

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Test
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

ksp {
    arg("vaadindsl.mode", "QUARKUS")
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
