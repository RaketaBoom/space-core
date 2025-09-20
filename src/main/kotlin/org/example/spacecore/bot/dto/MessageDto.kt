package org.example.spacecore.bot.dto

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.Message

data class MessageDto(
    val chatId: Long,
    val userId: Long,
    val messageId: Int = 0,
    val data: String = "",
    val text: String = "",
    val userName: String = "",
    val firstName: String = "",
    val lastName: String = ""
)

fun createMessageDto(callbackQuery: CallbackQuery): MessageDto {
    return MessageDto(
        callbackQuery.message.chatId,
        callbackQuery.from.id,
        callbackQuery.message.messageId,
        callbackQuery.data ?: "",
        "",
        callbackQuery.from.userName ?: "",
        callbackQuery.from.firstName,
        callbackQuery.from.lastName ?: ""
    )
}

fun createMessageDto(message: Message): MessageDto {
    return MessageDto(
        message.chatId,
        message.from.id,
        message.messageId,
        "",
        message.text ?: "",
        message.from.userName ?: "",
        message.from.firstName,
        message.from.lastName ?: ""
    )
}