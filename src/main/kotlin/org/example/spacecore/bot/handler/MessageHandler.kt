package org.example.spacecore.bot.handler

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.AdminText
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.text.MenuText
import org.example.spacecore.bot.text.ReportText
import org.example.spacecore.bot.util.LogUtil
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createProfileMessage
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@Component
class MessageHandler(
    private val profileService: ProfileService,
    private val userStateService: UserStateService,
    private val callbackHandler: CallbackHandler
) {

    fun handleMessage(message: Message, telegramClient: TelegramClient): List<SendMessage> {
        val messageDto = MessageDto(message)
        val currentState = userStateService.getCurrentState(messageDto.userId)

        return when {
            message.hasText() -> handleTextMessage(messageDto, currentState, telegramClient)
            message.hasPhoto() -> handlePhotoMessage(messageDto, message, currentState, telegramClient)
            else -> listOf()
        }
    }

    private fun handleCommand(
        msg: MessageDto,
        state: UserState,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        val command = msg.text.split(" ")[0].substring(1).lowercase()

        return when (command) {
            "start" -> handleStart(msg, telegramClient)
            "profile" -> callbackHandler.handleMyProfile(msg, telegramClient)
            else -> callbackHandler.handleMenu(msg, telegramClient)
        } as List<SendMessage>
    }

    private fun handleTextMessage(
        msg: MessageDto,
        state: UserState,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        return if (msg.text.startsWith("/")) {
            handleCommand(msg, state, telegramClient)
        } else
            when (state) {
                UserState.START -> handleStart(msg, telegramClient)
                UserState.ENTERING_NAME -> handleName(msg, telegramClient)
                UserState.ENTERING_AGE -> handleAge(msg, telegramClient)
                UserState.ENTERING_DESCRIPTION -> handleDescription(msg, telegramClient)
                UserState.REPORT -> handleReportEnded(msg, telegramClient)
                UserState.REPLY_REPORT -> handleReplyReport(msg, telegramClient)
                UserState.DISABLED -> callbackHandler.handleActive(msg, telegramClient, true)
                else -> callbackHandler.handleMenu(msg, telegramClient)
            }
    }

    private fun handleStart(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "start");
        telegramClient.execute(AdminText.newUserAdmin(msg, callbackHandler.adminId, profileService.getProfilesCount()))

        val checkUsername = callbackHandler.checkAndEditUsername(msg)
        if (checkUsername.isNotEmpty())
            return checkUsername

        userStateService.updateState(msg.userId, UserState.ENTERING_NAME)

        val scheduler = Executors.newScheduledThreadPool(2)
        scheduler.schedule({
            try {
                MessageUtil.deleteMessage(msg, telegramClient)
            } catch (e: Exception) {
                LogUtil.log(msg, "Failed to delete message: ${e.message}")
            }
        }, 3, TimeUnit.SECONDS)
        return FormText.start(msg)
    }

    private fun handleName(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "handleName");
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        if (msg.text.length > 50) {
            MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)
            MessageUtil.deleteMessage(msg, telegramClient)
            return FormText.nameError(msg)
        }
        profileService.updateName(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.ENTERING_AGE)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

        return callbackHandler.getMessageOrMyProfile(!editing, FormText.age(msg), msg, telegramClient)
    }

    private fun handleAge(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "handleAge");
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val age = msg.text.toIntOrNull()
        return if (age != null && age in 18..100) {
            profileService.updateAge(msg.userId, age)
            userStateService.updateState(msg.userId, UserState.SELECTING_GENDER)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

            callbackHandler.getMessageOrMyProfile(!editing, FormText.gender(msg), msg, telegramClient)
        } else {
            FormText.ageError(msg)
        }
    }

    private fun handleDescription(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "handleDescription");
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        if (msg.text.length > 800) {
            MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)
            return FormText.descriptionError(msg)
        }
        profileService.updateDescription(msg.userId, msg.text)
        userStateService.updateState(msg.userId, UserState.UPLOADING_PHOTO)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

        return callbackHandler.getMessageOrMyProfile(!editing, FormText.photo(msg), msg, telegramClient)
    }

    private fun handlePhotoMessage(
        msg: MessageDto,
        message: Message,
        state: UserState,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        LogUtil.log(msg, "handlePhotoMessage");
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        if (state == UserState.UPLOADING_PHOTO) {
            val photo = message.photo.last()
            val messageId = message.messageId
            profileService.updatePhoto(msg.userId, photo.fileId)
            userStateService.updateState(msg.userId, UserState.SELECTING_VIBE)

            MessageUtil.deleteMessage(msg, telegramClient)
            MessageUtil.deleteMessage(msg.userId, messageId - 1, telegramClient)

            if (!editing) {
                telegramClient.execute(FormText.vibe(msg))
            } else {
                userStateService.removeTempData(msg.userId, "edit")
                userStateService.updateState(msg.userId, UserState.MY_PROFILE)

                val profile = profileService.getOrCreateProfile(msg.userId)
                telegramClient.execute(createProfileMessage(msg, profile, true))
            }
        }
        return emptyList()
    }

    private fun handleReportEnded(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "handleReportEnded");
        userStateService.updateState(msg.userId, UserState.MENU)

        MessageUtil.deleteMessage(msg, telegramClient)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId - 1, telegramClient)

        return ReportText.reportEnded(msg) + AdminText.reportAdmin(
            msg,
            callbackHandler.adminId,
            msg.text
        ) + MenuText.menu(msg)
    }

    //Admin

    private fun handleReplyReport(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        LogUtil.log(msg, "handleReplyReport");
        userStateService.updateState(msg.userId, UserState.MENU)

        val reportedId = (userStateService.getTempData(msg.userId)["reported_id"] as String?)?.toLong() ?: -1

        return AdminText.sendReportAdmin(reportedId, msg.text)
    }
}