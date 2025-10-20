package org.example.spacecore.bot.handler

import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.model.Vibe
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.AdminText
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createProfileMessage
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class NewFormHandler(
    private val userStateService: UserStateService,
    private val profileService: ProfileService,
    private val callbackHandler: CallbackHandler
) {

    @Callback("gender_")
    private fun handleGender(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val gender = Gender.fromString(msg.data.removePrefix("gender_"))
        profileService.updateGender(msg.userId, gender)
        userStateService.updateState(msg.userId, UserState.SELECTING_LOOKING_FOR)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        return callbackHandler.getMessageOrMyProfile(!editing, FormText.lookingFor(msg), msg, telegramClient)
    }

    @Callback("lookingFor_")
    private fun handleLookingForSelection(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val lookingFor = Gender.fromString(msg.data.removePrefix("lookingFor_"))
        profileService.updateLookingFor(msg.userId, lookingFor)
        userStateService.updateState(msg.userId, UserState.ENTERING_DESCRIPTION)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        return callbackHandler.getMessageOrMyProfile(!editing, FormText.description(msg), msg, telegramClient)
    }

    @Callback("vibe_")
    private fun handleVibeSelection(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        val vibeValue = msg.data.removePrefix("vibe_").toInt()
        val vibe = Vibe.fromInt(vibeValue)
        profileService.updateVibe(msg.userId, vibe)
        profileService.updateActivityStatus(msg.userId, true)
        userStateService.updateState(msg.userId, UserState.MY_PROFILE)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        userStateService.clearTempData(msg.userId)

        val profile = profileService.getOrCreateProfile(msg.userId)

        // Загружаем анкеты для просмотра
        callbackHandler.loadProfilesForBrowsing(msg, profile)
        telegramClient.execute(createProfileMessage(msg, profile, true))

        telegramClient.execute(AdminText.newProfileAdmin(msg, callbackHandler.adminId))
        telegramClient.execute(createProfileMessage(callbackHandler.adminId, profile, toAdmin = true))

        callbackHandler.addNewForm(msg, profile)

        return listOf()
    }
}