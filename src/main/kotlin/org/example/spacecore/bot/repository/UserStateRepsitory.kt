package org.example.spacecore.bot.repository

import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.model.UserStateEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime

@Repository
class UserStateRepository(
    private val jdbcTemplate: JdbcTemplate
) {

    private val userStateRowMapper = RowMapper { rs: ResultSet, _: Int ->
        UserStateEntity(
            id = rs.getLong("id"),
            telegramId = rs.getLong("telegram_id"),
            state = UserState.valueOf(rs.getString("state")),
            tempData = parseJsonToJson(rs.getString("temp_data")),
            createdAt = rs.getObject("created_at", LocalDateTime::class.java),
            updatedAt = rs.getObject("updated_at", LocalDateTime::class.java)
        )
    }

    fun findByTelegramId(telegramId: Long): UserStateEntity? {
        val sql = "SELECT * FROM user_states WHERE telegram_id = ?"
        return jdbcTemplate.query(sql, userStateRowMapper, telegramId).firstOrNull()
    }

    fun save(userState: UserStateEntity): UserStateEntity {
        val sql = """
            INSERT INTO user_states (telegram_id, state, temp_data, created_at, updated_at)
            VALUES (?, ?, ?::jsonb, ?, ?)
            RETURNING id
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()

        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, arrayOf("id"))
            ps.setLong(1, userState.telegramId)
            ps.setString(2, userState.state.name)
            ps.setString(3, convertMapToJson(userState.tempData))
            ps.setObject(4, userState.createdAt)
            ps.setObject(5, userState.updatedAt)
            ps
        }, keyHolder)

        return userState.copy(id = keyHolder.key?.toLong() ?: 0L)
    }

    fun updateState(telegramId: Long, state: UserState): Boolean {
        val sql = """
            UPDATE user_states 
            SET state = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, state.name, telegramId) > 0
    }

    fun updateTempData(telegramId: Long, tempData: Map<String, Any>): Boolean {
        val sql = """
            UPDATE user_states 
            SET temp_data = ?::jsonb, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, convertMapToJson(tempData), telegramId) > 0
    }

    fun updateStateAndData(telegramId: Long, state: UserState, tempData: Map<String, Any>): Boolean {
        val sql = """
            UPDATE user_states 
            SET state = ?, temp_data = ?::jsonb, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, state.name, convertMapToJson(tempData), telegramId) > 0
    }

    fun deleteByTelegramId(telegramId: Long): Boolean {
        val sql = "DELETE FROM user_states WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, telegramId) > 0
    }

    fun existsByTelegramId(telegramId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM user_states WHERE telegram_id = ?"
        return jdbcTemplate.queryForObject(sql, Int::class.java, telegramId) ?: 0 > 0
    }

    private fun convertMapToJson(data: Map<String, Any>): String {
        return if (data.isEmpty()) "{}" else {
            // Простая реализация для JSON
            val entries = data.entries.joinToString(", ") { "\"${it.key}\": \"${it.value}\"" }
            "{$entries}"
        }
    }

    private fun parseJsonToJson(json: String?): Map<String, Any> {
        if (json.isNullOrEmpty() || json == "{}") return emptyMap()

        return try {
            // Простой парсинг JSON вида {"key": "value", "key2": "value2"}
            json.removePrefix("{").removeSuffix("}")
                .split(",")
                .associate { part ->
                    val keyValue = part.split(":")
                    val key = keyValue[0].trim().removeSurrounding("\"")
                    val value = keyValue[1].trim().removeSurrounding("\"")
                    key to value
                }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}