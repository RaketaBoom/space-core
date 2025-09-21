package org.example.spacecore.bot.repository

import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.model.Vibe
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.LocalDateTime
import kotlin.math.floor

@Repository
class ProfileRepository(
    private val jdbcTemplate: JdbcTemplate,
    private val namedParameterJdbcTemplate: NamedParameterJdbcTemplate
) {

    private val profileRowMapper = RowMapper { rs: ResultSet, _: Int ->
        Profile(
            id = rs.getLong("id"),
            telegramId = rs.getLong("telegram_id"),
            username = rs.getString("username"),
            firstName = rs.getString("first_name"),
            lastName = rs.getString("last_name"),
            name = rs.getString("name"),
            age = rs.getInt("age"),
            gender = Gender.fromString(rs.getString("gender")),
            lookingFor = Gender.fromString(rs.getString("looking_for")),
            description = rs.getString("description"),
            photoId = rs.getString("photo_id"),
            vibe = Vibe.fromInt(rs.getInt("vibe")),
            isActive = rs.getBoolean("is_active"),
            createdAt = rs.getObject("created_at", LocalDateTime::class.java)
        )
    }

    fun findByTelegramId(telegramId: Long): Profile? {
        val sql = "SELECT * FROM profiles WHERE telegram_id = ?"
        return jdbcTemplate.query(sql, profileRowMapper, telegramId).firstOrNull()
    }

    fun findById(id: Long): Profile? {
        val sql = "SELECT * FROM profiles WHERE id = ?"
        return jdbcTemplate.query(sql, profileRowMapper, id).firstOrNull()
    }

    fun existsByTelegramId(telegramId: Long): Boolean {
        val sql = "SELECT COUNT(*) FROM profiles WHERE telegram_id = ?"
        return jdbcTemplate.queryForObject(sql, Int::class.java, telegramId) ?: 0 > 0
    }

    fun save(profile: Profile): Profile {
        return if (profile.id == 0L) {
            insert(profile)
        } else {
            update(profile)
        }
    }

    // Обновление только определенных полей
    fun updatePartial(telegramId: Long, updates: Map<String, Any>): Boolean {
        if (updates.isEmpty()) return false

        val setClauses = updates.keys.joinToString(", ") { "$it = ?" }
        val values = updates.values.toList() + telegramId

        val sql = "UPDATE profiles SET $setClauses WHERE telegram_id = ?"

        return jdbcTemplate.update(sql, *values.toTypedArray()) > 0
    }

    // Обновление имени
    fun updateName(telegramId: Long, name: String): Boolean {
        val sql = """
            UPDATE profiles 
            SET name = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, name, telegramId) > 0
    }

    // Обновление username, first_name, last_name
    fun updateUserName(telegramId: Long, userName: String, firstName: String, lastName: String): Boolean {
        val sql = """
            UPDATE profiles 
            SET username = ?, first_name = ?, last_name = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, userName, firstName, lastName, telegramId) > 0
    }

    // Обновление возраста
    fun updateAge(telegramId: Long, age: Int): Boolean {
        val sql = """
            UPDATE profiles 
            SET age = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql, age, telegramId) > 0
    }

    // Обновление предпочтений
    fun updateLookingFor(telegramId: Long, lookingFor: Gender): Boolean {
        val sql = """
            UPDATE profiles 
            SET looking_for = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql,  lookingFor.name, telegramId) > 0
    }

    // Обновление Вайба
    fun updateVibe(telegramId: Long, vibe: Vibe): Boolean {
        val sql = """
            UPDATE profiles 
            SET vibe = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE telegram_id = ?
        """.trimIndent()

        return jdbcTemplate.update(sql,  vibe.value, telegramId) > 0
    }

    // Обновление описания
    fun updateDescription(telegramId: Long, description: String): Boolean {
        val sql = "UPDATE profiles SET description = ?, updated_at = CURRENT_TIMESTAMP WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, description, telegramId) > 0
    }

    // Обновление фото
    fun updatePhoto(telegramId: Long, photoId: String): Boolean {
        val sql = "UPDATE profiles SET photo_id = ?, updated_at = CURRENT_TIMESTAMP WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, photoId, telegramId) > 0
    }

    // Обновление пола
    fun updateGender(telegramId: Long, gender: Gender): Boolean {
        val sql = "UPDATE profiles SET gender = ?, updated_at = CURRENT_TIMESTAMP WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, gender.name, telegramId) > 0
    }

    // Обновление статуса активности
    fun updateActivityStatus(telegramId: Long, isActive: Boolean): Boolean {
        val sql = "UPDATE profiles SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, isActive, telegramId) > 0
    }

    // Обновление username
    fun updateUsername(telegramId: Long, username: String?): Boolean {
        val sql = "UPDATE profiles SET username = ?, updated_at = CURRENT_TIMESTAMP WHERE telegram_id = ?"
        return jdbcTemplate.update(sql, username, telegramId) > 0
    }

    private fun insert(profile: Profile): Profile {
        val sql = """
            INSERT INTO profiles (
                telegram_id, username, first_name, last_name, name, age, 
                gender, looking_for, description, photo_id, vibe, is_active, created_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val keyHolder = GeneratedKeyHolder()
        jdbcTemplate.update({ connection ->
            val ps = connection.prepareStatement(sql, arrayOf("id"))
            ps.setLong(1, profile.telegramId)
            ps.setString(2, profile.username)
            ps.setString(3, profile.firstName)
            ps.setString(4, profile.lastName)
            ps.setString(5, profile.name)
            ps.setInt(6, profile.age)
            ps.setString(7, profile.gender.name)
            ps.setString(8, profile.lookingFor.name)
            ps.setString(9, profile.description)
            ps.setString(10, profile.photoId)
            ps.setInt(11, profile.vibe.value)
            ps.setBoolean(12, profile.isActive)
            ps.setObject(13, profile.createdAt)
            ps
        }, keyHolder)

        return profile.copy(id = keyHolder.key?.toLong() ?: 0L)
    }

    private fun update(profile: Profile): Profile {
        val sql = """
            UPDATE profiles SET
                name = ?, age = ?,
                gender = ?, looking_for = ?, description = ?, photo_id = ?, 
                vibe = ?, is_active = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
        """.trimIndent()

        jdbcTemplate.update(sql,
            profile.name,
            profile.age,
            profile.gender.name,
            profile.lookingFor.name,
            profile.description,
            profile.photoId,
            profile.vibe.value,
            profile.isActive,
            profile.id
        )

        return profile
    }

    fun findMatchingProfiles(age: Int, vibe: Int, lookingFor: Gender, excludeTelegramId: Long, level: Int = 0): List<Profile> {
        val minAge = age - floor((level / 2).toDouble())
        val maxAge = age + floor((level / 2).toDouble())
        val minVibe = vibe - level
        val maxVibe = vibe + level
        val sql = """
            SELECT * FROM profiles 
            WHERE is_active = true 
            AND gender = ?
            AND telegram_id != ?
			AND age between ? and ?
			AND vibe between ? and ?
            LIMIT 50
        """.trimIndent()

        return jdbcTemplate.query(sql, profileRowMapper, lookingFor.name, excludeTelegramId, minAge, maxAge, minVibe, maxVibe)
    }
}