package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.Profile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

fun createSendMessage(msg: MessageDto, text: String, replyMarkup: InlineKeyboardMarkup? = null): SendMessage {
    return when (replyMarkup) {
        null -> SendMessage.builder()
            .chatId(msg.chatId.toString())
            .text(text)
            .build()
        else -> SendMessage.builder()
            .chatId(msg.chatId.toString())
            .text(text)
            .replyMarkup(replyMarkup)
            .build()
    }
}

fun createProfileMessage(msg: MessageDto, profile: Profile, myProfile: Boolean = false): SendMessage {
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

    return createSendMessage(msg, messageText, keyboard)
}

fun createUser(msg: MessageDto): User {
    return User.builder().id(msg.userId).userName(msg.userName).firstName(msg.firstName).lastName(msg.lastName).isBot(false).build()
}