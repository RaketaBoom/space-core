package org.example.spacecore.bot.handler

import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.model.Gender
import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.model.Vibe
import org.example.spacecore.bot.service.MatchService
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.util.MessageUtil
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.util.concurrent.ConcurrentHashMap
import org.example.spacecore.bot.util.createProfileMessage
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.objects.User

@Component
class CallbackHandler(
    private val profileService: ProfileService,
    private val matchService: MatchService,
    private val userStateService: UserStateService
) {
    private val browsingQueue = ConcurrentHashMap<Long, MutableList<Profile>>()

    fun handleCallback(callbackQuery: CallbackQuery, telegramClient: TelegramClient): List<SendMessage> {
        val data = callbackQuery.data
        val userId = callbackQuery.from.id
        val chatId = callbackQuery.message.chatId
        val messageId = callbackQuery.message.messageId

        return when {
            data.startsWith("gender_") -> handleGenderSelection(messageId, userId, chatId, data, telegramClient)
            data.startsWith("looking_") -> handleLookingForSelection(messageId,userId, chatId, data, telegramClient)
            data.startsWith("vibe_") -> handleVibeSelection(messageId,userId, chatId, data, telegramClient)
            data.startsWith("like_") -> handleLike(messageId,userId,chatId,  data, telegramClient)
            data.startsWith("dislike_") -> handleDislike(messageId,userId,chatId, telegramClient)
            data.startsWith("match_") -> handleMatch(messageId,userId,chatId,  data, telegramClient)
            data == "profiles" -> handleProfiles(messageId,userId, chatId, data, telegramClient)
            data == "menu" -> handleMenu(messageId,userId, chatId, telegramClient)
            else -> listOf(createSendMessage(chatId, "Неизвестная команда"))
        }
    }

    private fun handleGenderSelection(
        messageId: Int,
        userId: Long,
        chatId: Long,
        data: String,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        val gender = Gender.fromString(data.removePrefix("gender_"))
        profileService.updateGender(userId, gender)
        userStateService.updateState(userId, UserState.SELECTING_LOOKING_FOR)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text("Кого вы ищете?")
            .replyMarkup(Keyboard.lookingFor())
            .build()

        return listOf(message)
    }

    private fun handleLookingForSelection(
        messageId: Int,
        userId: Long,
        chatId: Long,
        data: String,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        val lookingFor = Gender.fromString(data.removePrefix("looking_"))
        profileService.updateLookingFor(userId, lookingFor)
        userStateService.updateState(userId, UserState.ENTERING_DESCRIPTION)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)

        return listOf(createSendMessage(chatId, "Расскажите о себе:"))
    }

    private fun handleVibeSelection(
        messageId: Int,
        userId: Long,
        chatId: Long,
        data: String,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        val vibeValue = data.removePrefix("vibe_").toInt()
        val vibe = Vibe.fromInt(vibeValue)
        profileService.updateVibe(userId, vibe)
        profileService.updateActivityStatus(userId, true)
        userStateService.updateState(userId, UserState.MY_PROFILE)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)

        val profile = profileService.getOrCreateProfile(userId)

        // Загружаем анкеты для просмотра
        loadProfilesForBrowsing(userId, profile)

        return listOf(createProfileMessage(chatId, profile, true))
    }

    private fun handleDislike(
        messageId: Int,
        userId: Long,
        chatId: Long,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        MessageUtil.deleteMessage(chatId, messageId,telegramClient)
        return getNextProfile(userId, chatId, telegramClient)
    }

    private fun handleMenu(
        messageId: Int,
        userId: Long,
        chatId: Long,
        telegramClient: TelegramClient
    ): List<SendMessage> {
        userStateService.updateState(userId, UserState.MENU)
        browsingQueue.remove(userId)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)

        return listOf(createSendMessage(chatId, "Главное меню", Keyboard.menu()))
    }

    private fun handleProfiles(messageId: Int, userId: Long, chatId: Long, data: String, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(userId, UserState.BROWSING_PROFILES)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)
        return getNextProfile(userId, userId, telegramClient)
    }

    private fun handleLike(messageId: Int, userId: Long, chatId: Long,  data: String, telegramClient: TelegramClient): List<SendMessage> {
        val likedUserId = data.removePrefix("like_").toLong()
        val userProfile = profileService.getOrCreateProfile(userId )

        val notification = matchService.createLikeNotification(userProfile, likedUserId)

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)

        return listOf(notification) + getNextProfile(userId, telegramClient)
    }

    private fun handleMatch(messageId: Int, userId: Long, chatId: Long,  data: String, telegramClient: TelegramClient): List<SendMessage> {
        val matchedUserId = data.removePrefix("match_").toLong()

        MessageUtil.deleteMessage(chatId, messageId,telegramClient)
        return matchService.createMatchNotification(userId, matchedUserId)
    }



    private fun getNextProfile(userId: Long, telegramClient: TelegramClient): List<SendMessage> {
        val chatId = userId // Используем userId как chatId для упрощения
        return getNextProfile(userId, chatId, telegramClient)
    }

    private fun getNextProfile(userId: Long, chatId: Long, telegramClient: TelegramClient): List<SendMessage> {
        val queue = browsingQueue[userId] ?: mutableListOf()

        if (queue.isEmpty()) {
            // Загружаем еще анкет
            val userProfile = profileService.getOrCreateProfile(User(userId, "", false))
            loadProfilesForBrowsing(userId, userProfile)
        }

        val nextProfile = browsingQueue[userId]?.removeFirstOrNull()

        return if (nextProfile != null) {
            listOf(createProfileMessage(chatId, nextProfile))
        } else {
            listOf(createSendMessage(chatId, "😔 Анкеты закончились. Попробуйте позже!"))
        }
    }

    private fun loadProfilesForBrowsing(userId: Long, userProfile: Profile) {
        val matchingProfiles = profileService.findMatchingProfiles(userProfile)
        browsingQueue[userId] = matchingProfiles.toMutableList()
    }
}