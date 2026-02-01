plugins {
    kotlin("jvm")
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
    implementation(project(":library"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.1.0-1.0.29")
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
            artifactId = "processor"
            version = project.version.toString()

            from(components["java"])

            pom {
                name.set("Vaadin DSL Codegen Processor")
                description.set("KSP processor for generating Vaadin DSL factory classes")
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
