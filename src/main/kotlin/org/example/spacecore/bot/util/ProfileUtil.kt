package org.example.spacecore.bot.util

import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.Profile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

fun createSendMessage(chatId: Long, text: String, replyMarkup: InlineKeyboardMarkup? = null): SendMessage {
    return when (replyMarkup) {
        null -> SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .build()
        else -> SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .replyMarkup(replyMarkup)
            .build()
    }
}

fun createProfileMessage(chatId: Long, profile: Profile, myProfile: Boolean = false): SendMessage {
    val messageText = """
            ${if (myProfile) "Ваша анкета:\n" else ""}
            👤 ${profile.name}, ${profile.age}
            ${profile.gender}
            
            📝 ${profile.description}
            
            💫 Вайб: ${profile.vibe.value}/9
        """.trimIndent()

    val keyboard: InlineKeyboardMarkup = when(myProfile) {
        false -> Keyboard.profile(profile)
        true -> Keyboard.myProfile()
    }

    return createSendMessage(chatId, messageText, keyboard)
}