package com.github.fenrur.vaadindslcodegen

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * Spring ApplicationContext holder for Vaadin DSL.
 *
 * This component allows the generated DSL functions to retrieve
 * Spring beans from the application context.
 *
 * This class is only used when the plugin is configured in Spring mode.
 */
@Component
class VaadinDslApplicationContextHolder : ApplicationContextAware {

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        context = applicationContext
    }

    companion object {
        private lateinit var context: ApplicationContext

        @JvmStatic
        fun getContext(): ApplicationContext = context

        @JvmStatic
        fun <T> getBean(beanClass: Class<T>): T = context.getBean(beanClass)
    }
}
