package org.example.spacecore.bot.callback

import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.callback.annotations.Command
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import kotlin.reflect.full.declaredFunctions

@Component
class HandlerScannerOnStartup : ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private lateinit var handlerRegistry: HandlerRegistry

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val context = event.applicationContext
        scanAllBeansForCallbacks(context)
    }

    private fun scanAllBeansForCallbacks(context: org.springframework.context.ApplicationContext) {
        context.beanDefinitionNames.forEach { beanName ->
            val bean = context.getBean(beanName)
            scanBean(bean, beanName)
        }
    }

    private fun scanBean(bean: Any, beanName: String) {
        val beanClass = bean::class

        beanClass.declaredFunctions.forEach { function ->
            function.annotations.forEach { annotation ->
                when (annotation) {
                    is Callback -> {
                        handlerRegistry.registerCallback(annotation.data, bean, function, annotation)
                        println("📋 Callback '${annotation.data}' -> $beanName.${function.name}()")
                    }
                    is Command -> {
                        handlerRegistry.registerCommand(annotation.data, bean, function, annotation)
                        println("📋 Command '${annotation.data}' -> $beanName.${function.name}()")
                    }
                }
            }
        }
    }
}