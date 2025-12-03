package org.example.spacecore.bot.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDateTime

data class UserStateEntity(
    val id: Long = 0,
    val telegramId: Long,
    val state: UserState,
    val tempData: Map<String, Any> = emptyMap(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private val objectMapper = jacksonObjectMapper()

        fun fromJsonString(json: String?): Map<String, Any> {
            return if (json.isNullOrEmpty()) {
                emptyMap()
            } else {
                try {
                    objectMapper.readValue(json, Map::class.java) as Map<String, Any>
                } catch (e: Exception) {
                    emptyMap()
                }
            }
        }

        fun toJsonString(data: Map<String, Any>): String {
            return try {
                objectMapper.writeValueAsString(data)
            } catch (e: Exception) {
                "{}"
            }
        }
    }
}