package org.example.spacecore.bot.handler

import org.telegram.telegrambots.meta.api.objects.User
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.util.MessageUtil
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.concurrent.ConcurrentHashMap


@Component
class MessageHandler(
    private val profileService: ProfileService,
    private val userStateService: UserStateService
) {
    private val userStates = ConcurrentHashMap<Long, UserState>()
    private val userTempData = ConcurrentHashMap<Long, MutableMap<String, Any>>()

    fun handleMessage(message: Message, telegramClient: TelegramClient): List<SendMessage> {
        val chatId = message.chatId
        val userId = message.from.id
        val currentState = userStateService.getCurrentState(userId)

        return when {
            message.hasText() -> handleTextMessage(message, currentState, userId, telegramClient)
            message.hasPhoto() -> handlePhotoMessage(message, currentState, userId, telegramClient)
            else -> listOf(createSendMessage(chatId.toString(), "Пожалуйста, используйте текстовые сообщения или фото"))
        }
    }

    private fun handleTextMessage(
        message: Message,
        state: UserState,
        userId: Long,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        val text = message.text
        val chatId = message.chatId
        val messageId = message.messageId

        return when (state) {
            UserState.START -> handleStart(messageId, chatId, userId, telegramClient, message)
            UserState.ENTERING_NAME -> handleName(messageId, chatId, userId, text, telegramClient, message)
            UserState.ENTERING_AGE -> handleAge(messageId, chatId, userId, text, telegramClient)
            UserState.ENTERING_DESCRIPTION -> handleDescription(messageId, chatId, userId, text, telegramClient)
            else -> listOf(createSendMessage(chatId.toString(), "Неизвестная команда"))
        }
    }

    private fun handleStart(messageId: Int, chatId: Long, userId: Long, telegramClient: TelegramClient, message: Message): List<SendMessage> {
        val chat = message.chat
        userStateService.updateState(userId, UserState.ENTERING_NAME)
        profileService.updateUserName(userId,
            User.builder().id(userId).isBot(false).firstName(chat.firstName).lastName(chat.firstName).userName(chat.userName).build()
        )

        MessageUtil.deleteMessage(chatId, messageId, telegramClient)
        return listOf(createSendMessage(chatId.toString(), "Давайте создадим вашу анкету! Как вас зовут?"))
    }

    private fun handleName(messageId: Int, chatId: Long, userId: Long, name: String, telegramClient: TelegramClient, message: Message): List<SendMessage> {
        profileService.updateName(userId, name)
        userStateService.updateState(userId, UserState.ENTERING_AGE)

        MessageUtil.deleteMessage(chatId, messageId, telegramClient)
        MessageUtil.deleteMessage(chatId, messageId - 1, telegramClient)
        return listOf(createSendMessage(chatId.toString(), "Сколько вам лет?"))
    }

    private fun handleAge(messageId: Int, chatId: Long, userId: Long, ageText: String, telegramClient: TelegramClient): List<SendMessage> {
        val age = ageText.toIntOrNull()
        return if (age != null && age in 18..100) {
            profileService.updateAge(userId, age)
            userStateService.updateState(userId, UserState.SELECTING_GENDER)

            MessageUtil.deleteMessage(chatId, messageId, telegramClient)
            MessageUtil.deleteMessage(chatId, messageId - 1, telegramClient)
            val message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Выберите ваш пол:")
                .replyMarkup(createGenderKeyboard())
                .build()

            listOf(message)
        } else {
            listOf(createSendMessage(chatId.toString(), "Пожалуйста, введите корректный возраст (18-100)"))
        }
    }

    private fun handleDescription(messageId: Int, chatId: Long, userId: Long, description: String, telegramClient: TelegramClient): List<SendMessage> {
        profileService.updateDescription(userId, description)
        userStateService.updateState(userId, UserState.UPLOADING_PHOTO)

        MessageUtil.deleteMessage(chatId, messageId, telegramClient)
        MessageUtil.deleteMessage(chatId, messageId - 1, telegramClient)
        return listOf(createSendMessage(chatId.toString(), "Отправьте ваше фото:"))
    }

    private fun handlePhotoMessage(message: Message, state: UserState, userId: Long, telegramClient: TelegramClient): List<SendMessage> {
        if (state == UserState.UPLOADING_PHOTO) {
            val photo = message.photo.last()
            val messageId = message.messageId
            profileService.updatePhoto(userId, photo.fileId)
            userStateService.updateState(userId, UserState.SELECTING_VIBE)

            MessageUtil.deleteMessage(userId, messageId, telegramClient)
            MessageUtil.deleteMessage(userId, messageId - 1, telegramClient)
            val sendMessage = SendMessage.builder()
                .chatId(message.chatId.toString())
                .text("Выберите ваш вайб (0-9):")
                .replyMarkup(createVibeKeyboard())
                .build()

            return listOf(sendMessage)
        }
        return emptyList()
    }

    private fun createSendMessage(chatId: String, text: String): SendMessage {
        return SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build()
    }

    private fun createGenderKeyboard(): InlineKeyboardMarkup {
        val keyboard = listOf(
            InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text("👨 Мужской")
                    .callbackData("gender_MALE")
                    .build(),
                InlineKeyboardButton.builder()
                    .text("👩 Женский")
                    .callbackData("gender_FEMALE")
                    .build()
            ),
            InlineKeyboardRow(
                InlineKeyboardButton.builder()
                    .text("👥 Другой")
                    .callbackData("gender_OTHER")
                    .build()
            )
        )
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build()
    }

    private fun createVibeKeyboard(): InlineKeyboardMarkup {
        val keyboard = listOf(
            InlineKeyboardRow((0..4).map { vibeValue ->
                InlineKeyboardButton.builder()
                    .text("$vibeValue")
                    .callbackData("vibe_$vibeValue")
                    .build()
            }),
            InlineKeyboardRow((5..9).map { vibeValue ->
                InlineKeyboardButton.builder()
                    .text("$vibeValue")
                    .callbackData("vibe_$vibeValue")
                    .build()
            })
        )
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build()
    }
}