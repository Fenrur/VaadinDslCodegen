pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
    }
}

rootProject.name = "vaadin-dsl-codegen"

include("library")
include("processor")
include("examples:quarkus")
include("examples:spring")
