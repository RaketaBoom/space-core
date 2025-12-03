package org.example.spacecore.bot.callback

import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.callback.annotations.Command
import org.springframework.stereotype.Component
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible

@Component
class HandlerRegistry {

    private val callbacks = mutableMapOf<String, CallbackMetadata>()
    private val commands = mutableMapOf<String, CommandMetadata>()

    fun registerCallback(name: String, bean: Any, method: KFunction<*>, annotation: Callback) {
        callbacks[name] = CallbackMetadata(bean, method, annotation, bean::class.simpleName ?: "")
    }

    fun registerCommand(name: String, bean: Any, method: KFunction<*>, annotation: Command) {
        commands[name] = CommandMetadata(bean, method, annotation, bean::class.simpleName ?: "")
    }

    fun getCallback(name: String): CallbackMetadata? = callbacks[name]
    fun getCommand(name: String): CommandMetadata? = commands[name]
    fun getAllCallbacks(): Map<String, CallbackMetadata> = callbacks.toMap()
    fun getAllCommands(): Map<String, CommandMetadata> = commands.toMap()
    fun getCallbackCount(): Int = callbacks.size + commands.size

    fun invokeCallback(name: String, vararg args: Any?): Any? {
        val metadata = callbacks[name] ?: throw IllegalArgumentException("Callback '$name' not found")
        return metadata.invoke(*args)
    }

    fun invokeCommand(name: String, vararg args: Any?): Any? {
        val metadata = commands[name] ?: throw IllegalArgumentException("Command '$name' not found")
        return metadata.invoke(*args)
    }
}

data class CallbackMetadata(
    val bean: Any,
    val method: KFunction<*>,
    val annotation: Callback,
    val className: String
) {
    fun invoke(vararg args: Any?): Any? {
        method.isAccessible = true
        return method.call(bean, *args)
    }
}

data class CommandMetadata(
    val bean: Any,
    val method: KFunction<*>,
    val annotation: Command,
    val className: String
) {
    fun invoke(vararg args: Any?): Any? {
        method.isAccessible = true
        return method.call(bean, *args)
    }
}