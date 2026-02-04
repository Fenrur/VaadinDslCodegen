import com.github.fenrur.vaadin.codegen.VaadinDslCodegenExtension.Mode

plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.vaadin") version "24.6.3"
    id("com.github.fenrur.vaadin-codegen") // Applies KSP automatically
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
    maven { url = uri("https://jitpack.io") }
}

val vaadinVersion = "24.6.3"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencyManagement {
    imports {
        mavenBom("com.vaadin:vaadin-bom:$vaadinVersion")
    }
}

dependencies {
    // Vaadin Spring Boot
    implementation("com.vaadin:vaadin-spring-boot-starter")

    // Our library
    implementation(project(":library"))
    ksp(project(":processor"))

    // Signal library for @ExposeSignal
    implementation("com.github.fenrur:signal:1.0.0")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

vaadinDslCodegen {
    mode = Mode.SPRING
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

vaadin {
    pnpmEnable = true
}
