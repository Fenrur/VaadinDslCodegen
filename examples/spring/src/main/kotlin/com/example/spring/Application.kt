package com.example.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.example.spring", "com.github.fenrur.vaadin.codegen"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
