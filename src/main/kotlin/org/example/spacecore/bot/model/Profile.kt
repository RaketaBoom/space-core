package org.example.spacecore.bot.model

import java.time.LocalDateTime

data class Profile(
    val id: Long = 0,
    val telegramId: Long,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val name: String,
    val age: Int,
    val gender: Gender,
    val lookingFor: Gender,
    val description: String,
    val photoId: String,
    val vibe: Vibe,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class Gender {
    MALE, FEMALE, OTHER;

    companion object {
        fun fromString(value: String): Gender {
            return values().find { it.name == value } ?: OTHER
        }
    }
}

enum class Vibe(val value: Int) {
    ONE(1), TWO(2), THREE(3), FOUR(4),
    FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), ;

    val smiles = arrayOf("\uD83E\uDDD0", "\uD83D\uDEF9", "\uD83D\uDC68\u200D\uD83C\uDFA8", "\uD83D\uDCAA", "\uD83C\uDFB8", "☺\uFE0F", "\uD83E\uDD17", "\uD83D\uDCBB", "\uD83E\uDDD1\u200D\uD83D\uDE80", "\uD83D\uDCDA")
    fun toSmile(): String {
        return smiles[value - 1]
    }

    companion object {

        fun fromInt(value: Int): Vibe {
            return values().find { it.value == value } ?: ONE
        }
    }
}