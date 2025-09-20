package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.MenuText
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
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


        fun editMessageForm(chatId: Long, messageId: Int,likedUserId: Long, telegramClient: TelegramClient) {
            try {
                val editMessage = EditMessageCaption.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .caption("")
                    .replyMarkup(Keyboard.openForm(likedUserId))
                    .build()

                telegramClient.execute(editMessage)
            } catch (e: TelegramApiException) {
                System.err.println("Error deleting message: " + e.message)
            }
        }
    }
}