plugins {
    kotlin("jvm") version "2.1.0"
    `java-gradle-plugin`
    `maven-publish`
}

group = "com.github.fenrur.vaadin-codegen"
version = System.getenv("VERSION") ?: "1.0.0"

repositories {
    mavenCentral()
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
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.1.0-1.0.29")
}

gradlePlugin {
    plugins {
        create("vaadinDslCodegen") {
            id = "com.github.fenrur.vaadin-codegen"
            implementationClass = "com.github.fenrur.vaadin.codegen.VaadinDslCodegenPlugin"
            displayName = "Vaadin DSL Codegen"
            description = "Gradle plugin for configuring Vaadin DSL code generation"
        }
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "gradle-plugin"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Vaadin DSL Codegen Gradle Plugin")
                description.set("Gradle plugin for configuring Vaadin DSL code generation")
                url.set("https://github.com/fenrur/vaadin-codegen")

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
                    url.set("https://github.com/fenrur/vaadin-codegen")
                    connection.set("scm:git:git://github.com/fenrur/vaadin-codegen.git")
                    developerConnection.set("scm:git:ssh://github.com/fenrur/vaadin-codegen.git")
                }
            }
        }
    }
}
