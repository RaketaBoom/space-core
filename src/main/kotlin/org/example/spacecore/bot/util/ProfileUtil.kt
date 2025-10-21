package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.Profile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup

fun createSendMessage(msg: MessageDto, text: String, replyMarkup: InlineKeyboardMarkup? = null, disableNotification: Boolean = false): SendMessage {
    return when (replyMarkup) {
        null -> SendMessage.builder()
            .chatId(msg.chatId.toString())
            .text(text)
            .disableNotification(disableNotification)
            .build()

        else -> SendMessage.builder()
            .chatId(msg.chatId.toString())
            .text(text)
            .replyMarkup(replyMarkup)
            .disableNotification(disableNotification)
            .build()
    }
}

fun createSendMessage(chatId: Long, text: String, replyMarkup: InlineKeyboardMarkup? = null, disableNotification: Boolean = false): SendMessage {
    return createSendMessage(MessageDto(chatId, 0), text, replyMarkup, disableNotification)
}

fun createSendPhoto(
    msg: MessageDto,
    photo_id: String,
    caption: String,
    replyMarkup: InlineKeyboardMarkup? = null,
    disableNotification: Boolean = false
): SendPhoto {
    return SendPhoto.builder()
        .chatId(msg.chatId.toString())
        .photo(InputFile(photo_id))
        .caption(caption)
        .replyMarkup(replyMarkup)
        .disableNotification(disableNotification)
        .build()
}

fun createSendPhoto(
    chatId: Long,
    photo_id: String,
    caption: String,
    replyMarkup: InlineKeyboardMarkup? = null
): SendPhoto {
    return createSendPhoto(MessageDto(chatId, 0), photo_id, caption, replyMarkup)
}

fun createProfileMessage(
    chatId: Long,
    profile: Profile,
    myProfile: Boolean = false,
    editing: Boolean = false,
    toAdmin: Boolean = false
): SendPhoto {
    return createProfileMessage(MessageDto(chatId, chatId), profile, myProfile, editing = editing, toAdmin = toAdmin)
}

fun profileMessageText(profile: Profile, myProfile: Boolean = false, toAdmin: Boolean = false): String {
    return """
${if (toAdmin) "Анкета пользователя:" else ""}${if (myProfile) "Ваша анкета:\n" else ""}
${profile.name}, ${profile.age}
${profile.description}
${if (myProfile) "Вайб: ${profile.vibe.value}" else ""}
        """.trimIndent()
}

fun createProfileMessage(
    msg: MessageDto,
    profile: Profile,
    myProfile: Boolean = false,
    editing: Boolean = false,
    toAdmin: Boolean = false
): SendPhoto {
    val messageText = profileMessageText(profile, myProfile, toAdmin)

    val keyboard: InlineKeyboardMarkup = when (myProfile) {
        false -> Keyboard.profile(profile)
        true -> if (!editing) Keyboard.myProfile() else Keyboard.editingProfile()
    }

    return createSendPhoto(msg, profile.photoId, messageText, keyboard, toAdmin)
}

fun createUser(msg: MessageDto): User {
    return User.builder().id(msg.userId).userName(msg.userName).firstName(msg.firstName).lastName(msg.lastName)
        .isBot(false).build()
}