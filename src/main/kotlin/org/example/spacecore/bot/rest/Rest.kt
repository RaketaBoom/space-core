package org.example.spacecore.bot.rest

import org.example.spacecore.bot.callback.HandlerService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/handlers")
class HandlerController(private val handlerService: HandlerService) {

    @GetMapping
    fun listHandlers(): Map<String, Any> {
        return mapOf("handlers" to handlerService.listAllHandlers())
    }

    @PostMapping("/callback/{name}")
    fun executeCallback(
        @PathVariable name: String,
        @RequestBody params: Map<String, Any>
    ): Map<String, Any> {
        return try {
            val result = handlerService.executeCallback(name, params["data"] ?: "")
            mapOf("success" to true, "result" to result)
        } catch (e: Exception) {
            mapOf("success" to false, "error" to e.message)
        } as Map<String, Any>
    }
}