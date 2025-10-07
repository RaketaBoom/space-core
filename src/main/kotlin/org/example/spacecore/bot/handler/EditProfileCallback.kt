package org.example.spacecore.bot.handler

import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.text.MenuText
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createProfileMessage
import org.example.spacecore.bot.util.createUser
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class EditProfileCallback(
    private val userStateService: UserStateService,
    private val profileService: ProfileService,
    private val callbackHandler: CallbackHandler
) {

    //Редактирование профиля
    @Callback("editing")
    private fun handleProfiles(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.MY_PROFILE)
        if (!MessageUtil.editMessageProfile(msg.chatId, msg.messageId, telegramClient)) {
            MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)

            val profile = profileService.getOrCreateProfile(msg.userId)
            telegramClient.execute(createProfileMessage(msg, profile, true, editing = true))
        }

        return listOf()
    }

    @Callback("edit")
    private fun handleEdit(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val checkUsername = callbackHandler.checkAndEditUsername(msg)
        if (checkUsername.isNotEmpty())
            return checkUsername

        userStateService.updateState(msg.userId, UserState.ENTERING_NAME)
        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.editProfile(msg)
    }

    @Callback("changeVibe")
    private fun handleChangeVibe(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.SELECTING_VIBE, "edit",true)

        MessageUtil.deleteMessage(msg, telegramClient)

        telegramClient.execute(FormText.vibe(msg))
        return emptyList()
    }

    @Callback("changeName")
    private fun handleChangeName(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_NAME, "edit", true)
        profileService.updateUserName(msg.userId, createUser(msg))

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.changeName(msg)
    }

    @Callback("changeAge")
    private fun handleChangeAge(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_AGE, "edit", true)

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.age(msg)
    }

    @Callback("changePhoto")
    private fun handleChangePhoto(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.UPLOADING_PHOTO, "edit", true)

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.photo(msg, true)
    }

    @Callback("changeDescription")
    private fun handleDescription(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_DESCRIPTION, "edit", true)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return FormText.description(msg)
    }

    @Callback("profileInactive")
    private fun handleInactive(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        profileService.updateActivityStatus(msg.userId, false)
        userStateService.updateState(msg.userId, UserState.DISABLED)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return MenuText.inactive(msg)
    }
}