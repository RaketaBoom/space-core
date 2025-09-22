package org.example.spacecore.bot.handler

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.dto.createMessageDto
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createUser
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient


@Component
class MessageHandler(
    private val profileService: ProfileService,
    private val userStateService: UserStateService,
    private val callbackHandler: CallbackHandler
) {

    fun handleMessage(message: Message, telegramClient: TelegramClient): List<SendMessage> {
        val messageDto = createMessageDto(message)
        val currentState = userStateService.getCurrentState(messageDto.userId)

        return when {
            message.hasText() -> handleTextMessage(messageDto, currentState,  telegramClient)
            message.hasPhoto() -> handlePhotoMessage(messageDto,message, currentState,  telegramClient)
            else -> listOf()
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
            else -> callbackHandler.handleMenu(msg, telegramClient)
        }
    }

    private fun handleStart(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.ENTERING_NAME)
        profileService.updateUserName(msg.userId, createUser(msg))

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.start(msg)
    }

    private fun handleName(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        profileService.updateName(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.ENTERING_AGE)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

        return callbackHandler.getMessageOrMyProfile(!editing,FormText.age(msg), msg, telegramClient)
    }

    private fun handleAge(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val age = msg.text.toIntOrNull()
        return if (age != null && age in 18..100) {
            profileService.updateAge(msg.userId, age)
            userStateService.updateState(msg.userId, UserState.SELECTING_GENDER)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

            callbackHandler.getMessageOrMyProfile(!editing,FormText.gender(msg), msg, telegramClient)
        } else {
            FormText.errorAge(msg)
        }
    }

    private fun handleDescription(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        profileService.updateDescription(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.UPLOADING_PHOTO)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

        return callbackHandler.getMessageOrMyProfile(!editing,FormText.photo(msg), msg, telegramClient)
    }

    private fun handlePhotoMessage(msg: MessageDto,message: Message, state: UserState, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        return if (state == UserState.UPLOADING_PHOTO) {
            val photo = message.photo.last()
            val messageId = message.messageId
            profileService.updatePhoto(msg.userId, photo.fileId)
            userStateService.updateState(msg.userId, UserState.SELECTING_VIBE)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.userId, messageId - 1, telegramClient)

            callbackHandler.getMessageOrMyProfile(!editing,FormText.vibe(msg), msg, telegramClient)
        } else emptyList()
    }
}