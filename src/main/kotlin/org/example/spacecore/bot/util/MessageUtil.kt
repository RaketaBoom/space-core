package org.example.spacecore.bot.util

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

        fun deleteMultipleMessages(chatId: Long, messageIds: MutableList<Int>, telegramClient: TelegramClient) {
            for (messageId in messageIds) {
                try {
                    val deleteMessage: DeleteMessage? = DeleteMessage.builder()
                        .chatId(chatId.toString())
                        .messageId(messageId)
                        .build()

                    telegramClient.execute(deleteMessage)
                    Thread.sleep(100) // Небольшая задержка между запросами
                } catch (e: TelegramApiException) {
                    System.err.println("Error deleting message " + messageId + ": " + e.message)
                    Thread.currentThread().interrupt()
                } catch (e: InterruptedException) {
                    System.err.println("Error deleting message " + messageId + ": " + e.message)
                    Thread.currentThread().interrupt()
                }
            }
        }
    }
}