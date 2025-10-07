package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.Profile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
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

fun createSendMessage(chatId: Long, text: String, replyMarkup: InlineKeyboardMarkup? = null): SendMessage {
    return createSendMessage(MessageDto(chatId, 0), text,replyMarkup)
}

fun createSendPhoto(msg: MessageDto, photo_id: String, caption: String, replyMarkup: InlineKeyboardMarkup? = null): SendPhoto {
    return SendPhoto.builder()
            .chatId(msg.chatId.toString())
            .photo(InputFile(photo_id))
            .caption(caption)
            .replyMarkup(replyMarkup)
            .build()
}
fun createSendPhoto(chatId: Long, photo_id: String, caption: String, replyMarkup: InlineKeyboardMarkup? = null): SendPhoto {
    return createSendPhoto(MessageDto(chatId, 0),photo_id,caption,replyMarkup)
}

fun createProfileMessage(chatId: Long, profile: Profile, myProfile: Boolean = false): SendPhoto {
    return createProfileMessage(MessageDto(chatId, chatId), profile, myProfile)
}

fun profileMessageText(profile: Profile, myProfile: Boolean = false ): String{
    return """
            ${if (myProfile) "Ваша анкета:\n" else ""}
            ${profile.name}, ${profile.age}
            ${profile.description}
            ${if (myProfile) "Вайб: ${profile.vibe.value}" else ""}
        """.trimIndent()
}

fun createProfileMessage(msg: MessageDto, profile: Profile, myProfile: Boolean = false, editing: Boolean = false): SendPhoto {
    val messageText = profileMessageText(profile, myProfile)

    val keyboard: InlineKeyboardMarkup = when(myProfile) {
        false -> Keyboard.profile(profile)
        true -> if (!editing) Keyboard.myProfile() else Keyboard.editingProfile()
    }

    return createSendPhoto(msg, profile.photoId,messageText, keyboard)
}

fun createUser(msg: MessageDto): User {
    return User.builder().id(msg.userId).userName(msg.userName).firstName(msg.firstName).lastName(msg.lastName).isBot(false).build()
}