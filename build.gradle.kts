plugins {
    kotlin("jvm") version "2.1.0" apply false
}

allprojects {
    group = "com.github.fenrur.vaadin-codegen"
    version = System.getenv("VERSION") ?: "1.0.0"

    repositories {
        mavenCentral()
    }
}
