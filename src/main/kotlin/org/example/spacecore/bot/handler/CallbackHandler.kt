package org.example.spacecore.bot.handler

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.dto.createMessageDto
import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.model.Vibe
import org.example.spacecore.bot.service.MatchService
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.FormText
import org.example.spacecore.bot.text.MenuText
import org.example.spacecore.bot.util.AddUtil
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.TimedCacheMap
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.example.spacecore.bot.util.createProfileMessage
import org.example.spacecore.bot.util.createUser
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage

@Component
class CallbackHandler(
    private val profileService: ProfileService,
    private val matchService: MatchService,
    private val userStateService: UserStateService
) {
    private val browsingQueue = TimedCacheMap(userStateService)

    fun handleCallback(callbackQuery: CallbackQuery, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        val messageDto = createMessageDto(callbackQuery)

        return when {
            messageDto.data.startsWith("gender_") -> handleGender(messageDto, telegramClient)
            messageDto.data.startsWith("looking_") -> handleLookingForSelection(messageDto, telegramClient)
            messageDto.data.startsWith("vibe_") -> handleVibeSelection(messageDto, telegramClient)

            messageDto.data.startsWith("like_") -> handleLike(messageDto, telegramClient)
            messageDto.data.startsWith("dislike_") -> handleDislike(messageDto, telegramClient)
            messageDto.data.startsWith("match_") -> handleMatch(messageDto, telegramClient)

            messageDto.data == "profiles" -> handleProfiles(messageDto, telegramClient)
            messageDto.data == "my_profile" -> handleMyProfile(messageDto, telegramClient)
            messageDto.data == "menu" -> handleMenu(messageDto, telegramClient)
            messageDto.data.startsWith("open_") -> handleMyProfile(messageDto, telegramClient)

            messageDto.data == "edit" -> handleEdit(messageDto, telegramClient)
            messageDto.data == "change_vibe" -> handleChangeVibe(messageDto, telegramClient)
            messageDto.data == "change_name" -> handleChangeName(messageDto, telegramClient)
            messageDto.data == "change_age" -> handleChangeAge(messageDto, telegramClient)
            messageDto.data == "change_photo" -> handleChangePhoto(messageDto, telegramClient)
            messageDto.data == "change_description" -> handleDescription(messageDto, telegramClient)
            else -> handleMenu(messageDto, telegramClient)
        }
    }

    private fun handleGender(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val gender = Gender.fromString(msg.data.removePrefix("gender_"))
        profileService.updateGender(msg.userId, gender)
        userStateService.updateState(msg.userId, UserState.SELECTING_LOOKING_FOR)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        return getMessageOrMyProfile(!editing,FormText.lookingFor(msg), msg, telegramClient)
    }

    private fun handleLookingForSelection(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val editing: Boolean = ((userStateService.getTempData(msg.userId)["edit"] ?: "") as String).toBoolean()

        val lookingFor = Gender.fromString(msg.data.removePrefix("looking_"))
        profileService.updateLookingFor(msg.userId, lookingFor)
        userStateService.updateState(msg.userId, UserState.ENTERING_DESCRIPTION)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return getMessageOrMyProfile(!editing,FormText.description(msg), msg, telegramClient)
    }

    private fun handleVibeSelection(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        val vibeValue = msg.data.removePrefix("vibe_").toInt()
        val vibe = Vibe.fromInt(vibeValue)
        profileService.updateVibe(msg.userId, vibe)
        profileService.updateActivityStatus(msg.userId, true)
        userStateService.updateState(msg.userId, UserState.MY_PROFILE)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        userStateService.clearTempData(msg.userId)

        val profile = profileService.getOrCreateProfile(msg.userId)

        // Загружаем анкеты для просмотра
        loadProfilesForBrowsing(msg, profile)
        telegramClient.execute(createProfileMessage(msg, profile, true))

        return listOf()
    }

    private fun handleMyProfile(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
//        userStateService.updateState(msg.userId, UserState.MY_PROFILE)
//        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
//
//        val profile = profileService.getOrCreateProfile(msg.userId)
//        telegramClient.execute(createProfileMessage(msg, profile, true))

        AddUtil.generateAngels(profileService)
        return listOf()
    }

    fun handleMenu(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.MENU)
//        browsingQueue.remove(msg.userId)

        MessageUtil.deleteMessage(msg, telegramClient)

        return MenuText.menu(msg)
    }

    private fun handleProfiles(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.BROWSING_PROFILES)

        MessageUtil.deleteMessage(msg, telegramClient)
        getProfile(msg, telegramClient, false)
        return listOf()
    }

    private fun handleLike(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        profileService.updateUserName(msg.userId, createUser(msg))
        val profileId = msg.data.removePrefix("like_").toLong()
        val likedUserId = profileService.getTelegramId(profileId)
        val lastProfileId = (userStateService.getTempData(msg.userId)["profileId"] as String?)?.toLongOrNull() ?: 0
        if (profileId == lastProfileId) {
            val userProfile = profileService.getOrCreateProfile(msg.userId)

            MessageUtil.editMessageForm(msg.chatId, msg.messageId, likedUserId, telegramClient)

            matchService.sendLikeNotification(userProfile, likedUserId, telegramClient)

            getProfile(msg, telegramClient)
        }
        return listOf()
    }

    private fun handleDislike(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        val profileId = msg.data.removePrefix("dislike_").toLong()
        val likedUserId = profileService.getTelegramId(profileId)
        val lastProfileId = (userStateService.getTempData(msg.userId)["profileId"] as String?)?.toLongOrNull() ?: 0
        if (profileId == lastProfileId) {
            MessageUtil.editMessageForm(msg.chatId, msg.messageId, likedUserId, telegramClient)
            getProfile(msg, telegramClient)
        }
        return listOf()
    }

    private fun handleMatch(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val matchedUserId = profileService.getTelegramId(msg.data.removePrefix("match_").toLong())

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        return matchService.createMatchNotification(msg.userId, matchedUserId)
    }

    //Редактирование профиля
    private fun handleEdit(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.ENTERING_NAME)
        profileService.updateUserName(msg.userId, createUser(msg))

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.editProfile(msg)
    }

    private fun handleChangeVibe(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.SELECTING_VIBE, "edit",true)

        MessageUtil.deleteMessage(msg, telegramClient)

        return FormText.vibe(msg)
    }

    private fun handleChangeName(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_NAME, "edit", true)
        profileService.updateUserName(msg.userId, createUser(msg))

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.changeName(msg)
    }

    private fun handleChangeAge(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_AGE, "edit", true)

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.age(msg)
    }

    private fun handleChangePhoto(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.UPLOADING_PHOTO, "edit", true)

        MessageUtil.deleteMessage(msg, telegramClient)
        return FormText.photo(msg)
    }

    private fun handleDescription(msg: MessageDto, telegramClient: TelegramClient  ): List<BotApiMethodMessage> {
        userStateService.updateStateAndData(msg.userId, UserState.ENTERING_DESCRIPTION, "edit", true)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return FormText.description(msg)
    }

    //Функции-утилиты
    private fun getProfile(msg: MessageDto, telegramClient: TelegramClient, next: Boolean = true) {
        val queue = browsingQueue[msg.userId] ?: mutableListOf()

        if (queue.isEmpty()) {
            // Загружаем еще анкет
            val userProfile = profileService.getOrCreateProfile(createUser(msg))
            loadProfilesForBrowsing(msg, userProfile)
        }

        if (next) {
            browsingQueue[msg.userId]?.removeFirstOrNull()
//            browsingQueue.refreshTimer(msg.userId)
        }
        val nextProfileId = browsingQueue[msg.userId]?.firstOrNull()
        if (nextProfileId != null) {
            val nextProfile = profileService.getById(nextProfileId)
            if (nextProfile != null) {
                userStateService.putTempData(msg.userId, "profileId", nextProfileId)
                telegramClient.execute(createProfileMessage(msg, nextProfile))
            }
        } else {
            MenuText.formEnded(msg).forEach { response ->
                telegramClient.execute(response)
            }
        }
    }

    private fun loadProfilesForBrowsing(msg: MessageDto, userProfile: Profile) {
        var count = 0
        var level = (userStateService.getTempData(msg.userId)["level"] as String?)?.toInt() ?: -1
        var matchingProfiles = listOf<Long>()
        while (matchingProfiles.size < 15){
            count += 1
            if (count == 10)
                break
            level += 1
            if (level == 10)
                level = 0
            matchingProfiles = matchingProfiles + profileService.findMatchingProfiles(userProfile, level)
        }
        userStateService.putTempData(msg.userId,"level", level)
        browsingQueue[msg.userId] = matchingProfiles.toMutableList()
    }

    fun getMessageOrMyProfile(bool: Boolean, message:  List<SendMessage>, msg: MessageDto, telegramClient: TelegramClient ): List<SendMessage>{
        if (bool) {
            return message
        } else {
            userStateService.removeTempData(msg.userId, "edit")
            userStateService.updateState(msg.userId, UserState.MY_PROFILE)

            val profile = profileService.getOrCreateProfile(msg.userId)
            telegramClient.execute(createProfileMessage(msg, profile, true))
            return listOf()
        }
    }
}