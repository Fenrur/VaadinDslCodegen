package com.example.quarkus

import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces
import jakarta.enterprise.inject.spi.InjectionPoint
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * CDI producer for SLF4J Logger injection.
 */
@ApplicationScoped
class LoggerProducer {

    @Produces
    fun produceLogger(injectionPoint: InjectionPoint): Logger {
        return LoggerFactory.getLogger(injectionPoint.member.declaringClass)
    }
}
