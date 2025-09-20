package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient

class MessageUtil {
    companion object {
        fun deleteMessage(chatId: Long, messageId: Int, telegramClient: TelegramClient) {
            try {
                val deleteMessage: DeleteMessage? = DeleteMessage.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .build()

                telegramClient.execute(deleteMessage)
            } catch (e: TelegramApiException) {
                System.err.println("Error deleting message: " + e.message)
            }
        }

        fun deleteMessage(msg: MessageDto, telegramClient: TelegramClient) {
            deleteMessage(msg.chatId, msg.messageId, telegramClient)
        }

        fun deleteMultipleMessages(chatId: Long, messageIds: MutableList<Int>, telegramClient: TelegramClient) {
            for (messageId in messageIds) {
                deleteMessage(chatId, messageId, telegramClient)
            }
        }
    }
}