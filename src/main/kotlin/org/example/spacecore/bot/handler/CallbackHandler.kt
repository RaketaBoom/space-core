package org.example.spacecore.bot.handler

import org.example.spacecore.bot.callback.HandlerService
import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.MatchService
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.MenuText
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
    private val userStateService: UserStateService,
    private val handlerService: HandlerService
) {
    private val browsingQueue = TimedCacheMap(userStateService)

    val adminId: Long = 885172912

    fun handleCallback(callbackQuery: CallbackQuery, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        val messageDto = MessageDto(callbackQuery)

        val result = handlerService.executeCallback(messageDto.data, messageDto, telegramClient)
        return result as? List<BotApiMethodMessage> ?: listOf()
    }


    @Callback("myProfile")
    private fun handleMyProfile(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        userStateService.updateState(msg.userId, UserState.MY_PROFILE)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)

        val profile = profileService.getOrCreateProfile(msg.userId)
        telegramClient.execute(createProfileMessage(msg, profile, true))

        return listOf()
    }

    @Callback("menu")
    fun handleMenu(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.MENU)
//        browsingQueue.remove(msg.userId)

        MessageUtil.deleteMessage(msg, telegramClient)

        return MenuText.menu(msg)
    }

    @Callback("profiles")
    private fun handleProfiles(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.BROWSING_PROFILES)

        MessageUtil.deleteMessage(msg, telegramClient)
        getProfile(msg, telegramClient, false)
        return listOf()
    }

    @Callback("like_")
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

    @Callback("dislike_")
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

    @Callback("match_")
    private fun handleMatch(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val matchedUserId = profileService.getTelegramId(msg.data.removePrefix("match_").toLong())

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        return matchService.createMatchNotification(msg.userId, matchedUserId)
    }

    //Функции-утилиты
    fun getProfile(msg: MessageDto, telegramClient: TelegramClient, next: Boolean = true) {
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
        var nextProfile: Profile? = null
        while (nextProfile == null) {
            val nextProfileId = browsingQueue[msg.userId]?.firstOrNull()
            if (nextProfileId != null) {
                nextProfile = profileService.getById(nextProfileId)
                if (nextProfile != null) {
                    userStateService.putTempData(msg.userId, "profileId", nextProfileId)
                    telegramClient.execute(createProfileMessage(msg, nextProfile))
                }
            } else {
                MenuText.formEnded(msg).forEach { response ->
                    telegramClient.execute(response)
                }
                userStateService.putTempData(msg.userId, "level", 0)
                break
            }
        }
    }

    fun loadProfilesForBrowsing(msg: MessageDto, userProfile: Profile) {
        var count = 0
        var level = (userStateService.getTempData(msg.userId)["level"] as String?)?.toInt() ?: -1
        var matchingProfiles = listOf<Long>()
        while (matchingProfiles.size < 15) {
            count += 1
            if (count == 10)
                break
            level += 1
            if (level == 10)
                level = 0
            matchingProfiles = matchingProfiles + profileService.findMatchingProfiles(userProfile, level)
        }
        userStateService.putTempData(msg.userId, "level", level)
        browsingQueue[msg.userId] = matchingProfiles.toMutableList()
    }

    fun getMessageOrMyProfile(
        bool: Boolean,
        message: List<SendMessage>,
        msg: MessageDto,
        telegramClient: TelegramClient
    ): List<SendMessage> {
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