plugins {
    kotlin("jvm")
    `java-library`
    `maven-publish`
}

dependencies {
    // Spring is optional - only needed when using Spring mode
    // Using Spring 5.x for Java 11 compatibility
    compileOnly("org.springframework:spring-context:5.3.39")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
    withSourcesJar()
    withJavadocJar()
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
            artifactId = "library"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Vaadin DSL Codegen Library")
                description.set("Annotations for Vaadin DSL code generation")
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
