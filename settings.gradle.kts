pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.vaadin.com/vaadin-prereleases") }
    }

    // Include the gradle-plugin as a composite build so it can be used in examples
    includeBuild("gradle-plugin")
}

rootProject.name = "vaadin-dsl-codegen"

include("library")
include("processor")
// gradle-plugin is included via includeBuild in pluginManagement
include("examples:quarkus")
include("examples:spring")
