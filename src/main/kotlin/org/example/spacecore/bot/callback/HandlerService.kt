package org.example.spacecore.bot.callback

import org.springframework.stereotype.Service

@Service
class HandlerService(private val handlerRegistry: HandlerRegistry) {

    fun listAllHandlers(): Map<String, List<String>> {
        return mapOf(
            "callbacks" to handlerRegistry.getAllCallbacks().keys.toList(),
            "commands" to handlerRegistry.getAllCommands().keys.toList(),
        )
    }

    fun executeCallback(callbackName: String, vararg args: Any?): Any? {
        var callback = callbackName
        if (callback.indexOf("_") != -1){
            callback = callback.take(callback.indexOf("_") + 1)
        }
        return handlerRegistry.invokeCallback(callback, *args)
    }

    fun executeCommand(commandName: String, vararg args: Any?): Any? {
        return handlerRegistry.invokeCommand(commandName, *args)
    }
}