package org.example.spacecore.bot.handler

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.dto.createMessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.telegram.telegrambots.meta.api.objects.User
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createSendMessage
import org.example.spacecore.bot.util.createUser
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

    fun handleMessage(message: Message, telegramClient: TelegramClient): List<SendMessage> {
        val messageDto = createMessageDto(message)
        val currentState = userStateService.getCurrentState(messageDto.userId)

        return when {
            message.hasText() -> handleTextMessage(messageDto, currentState,  telegramClient)
            message.hasPhoto() -> handlePhotoMessage(messageDto,message, currentState,  telegramClient)
            else -> listOf(createSendMessage(messageDto, "Пожалуйста, используйте текстовые сообщения или фото"))
        }
    }

    private fun handleTextMessage(
        msg: MessageDto,
        state: UserState,
        telegramClient: TelegramClient
    ): List<SendMessage> {

        return when (state) {
            UserState.START -> handleStart(msg, telegramClient)
            UserState.ENTERING_NAME -> handleName(msg, telegramClient)
            UserState.ENTERING_AGE -> handleAge(msg,   telegramClient)
            UserState.ENTERING_DESCRIPTION -> handleDescription(msg,  telegramClient)
            else -> listOf(createSendMessage(msg, "Неизвестная команда"))
        }
    }

    private fun handleStart(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.ENTERING_NAME)
        profileService.updateUserName(msg.userId, createUser(msg))

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.start(msg)
    }

    private fun handleName(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        profileService.updateName(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.ENTERING_AGE)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)
        return FormText.name(msg)
    }

    private fun handleAge(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val age = msg.text.toIntOrNull()
        return if (age != null && age in 18..100) {
            profileService.updateAge(msg.userId, age)
            userStateService.updateState(msg.userId, UserState.SELECTING_GENDER)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

            listOf(createSendMessage(msg, "Выберите ваш пол:", Keyboard.genderKeyboard()))
        } else {
            listOf(createSendMessage(msg, "Пожалуйста, введите корректный возраст (18-100)"))
        }
    }

    private fun handleDescription(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        profileService.updateDescription(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.UPLOADING_PHOTO)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)
        return listOf(createSendMessage(msg, "Отправьте ваше фото:"))
    }

    private fun handlePhotoMessage(msg: MessageDto,message: Message, state: UserState, telegramClient: TelegramClient): List<SendMessage> {
        if (state == UserState.UPLOADING_PHOTO) {
            val photo = message.photo.last()
            val messageId = message.messageId
            profileService.updatePhoto(msg.userId, photo.fileId)
            userStateService.updateState(msg.userId, UserState.SELECTING_VIBE)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.userId, messageId - 1, telegramClient)

            return listOf(createSendMessage(msg, "Выберите ваш вайб (0-9):", Keyboard.vibeKeyboard()))
        }
        return emptyList()
    }
}