plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    implementation(gradleApi())
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

gradlePlugin {
    plugins {
        create("vaadinDslCodegen") {
            id = "com.github.fenrur.vaadin-dsl-codegen"
            implementationClass = "com.github.fenrur.vaadindslcodegen.VaadinDslCodegenPlugin"
            displayName = "Vaadin DSL Codegen"
            description = "Generates DSL factory classes and extension functions for Vaadin components"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "vaadin-dsl-codegen-gradle-plugin"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Vaadin DSL Codegen Gradle Plugin")
                description.set("Gradle plugin for generating Vaadin DSL factory classes")
                url.set("https://github.com/fenrur/vaadin-dsl-codegen")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("fenrur")
                        name.set("Livio TINNIRELLO")
                    }
                }

                scm {
                    url.set("https://github.com/fenrur/vaadin-dsl-codegen")
                    connection.set("scm:git:git://github.com/fenrur/vaadin-dsl-codegen.git")
                    developerConnection.set("scm:git:ssh://github.com/fenrur/vaadin-dsl-codegen.git")
                }
            }
        }
    }
}
