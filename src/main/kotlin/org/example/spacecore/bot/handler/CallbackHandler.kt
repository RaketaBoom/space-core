package org.example.spacecore.bot.handler

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.dto.createMessageDto
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
import org.example.spacecore.bot.util.createUser

@Component
class CallbackHandler(
    private val profileService: ProfileService,
    private val matchService: MatchService,
    private val userStateService: UserStateService
) {
    private val browsingQueue = ConcurrentHashMap<Long, MutableList<Profile>>()

    fun handleCallback(callbackQuery: CallbackQuery, telegramClient: TelegramClient): List<SendMessage> {
        val messageDto = createMessageDto(callbackQuery)

        return when {
            messageDto.data.startsWith("gender_") -> handleGenderSelection(messageDto, telegramClient)
            messageDto.data.startsWith("looking_") -> handleLookingForSelection(messageDto, telegramClient)
            messageDto.data.startsWith("vibe_") -> handleVibeSelection(messageDto, telegramClient)
            messageDto.data.startsWith("like_") -> handleLike(messageDto, telegramClient)
            messageDto.data.startsWith("dislike_") -> handleDislike(messageDto, telegramClient)
            messageDto.data.startsWith("match_") -> handleMatch(messageDto, telegramClient)
            messageDto.data == "profiles" -> handleProfiles(messageDto, telegramClient)
            messageDto.data == "menu" -> handleMenu(messageDto, telegramClient)
            else -> listOf(createSendMessage(messageDto, "Неизвестная команда"))
        }
    }

    private fun handleGenderSelection(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val gender = Gender.fromString(msg.data.removePrefix("gender_"))
        profileService.updateGender(msg.userId, gender)
        userStateService.updateState(msg.userId, UserState.SELECTING_LOOKING_FOR)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)
        val message = SendMessage.builder()
            .chatId(msg.chatId.toString())
            .text("Кого вы ищете?")
            .replyMarkup(Keyboard.lookingFor())
            .build()

        return listOf(message)
    }

    private fun handleLookingForSelection(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val lookingFor = Gender.fromString(msg.data.removePrefix("looking_"))
        profileService.updateLookingFor(msg.userId, lookingFor)
        userStateService.updateState(msg.userId, UserState.ENTERING_DESCRIPTION)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)

        return listOf(createSendMessage(msg, "Расскажите о себе:"))
    }

    private fun handleVibeSelection(msg: MessageDto, telegramClient: TelegramClient  ): List<SendMessage> {
        val vibeValue = msg.data.removePrefix("vibe_").toInt()
        val vibe = Vibe.fromInt(vibeValue)
        profileService.updateVibe(msg.userId, vibe)
        profileService.updateActivityStatus(msg.userId, true)
        userStateService.updateState(msg.userId, UserState.MY_PROFILE)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)

        val profile = profileService.getOrCreateProfile(msg.userId)

        // Загружаем анкеты для просмотра
        loadProfilesForBrowsing(msg, profile)

        return listOf(createProfileMessage(msg, profile, true))
    }

    private fun handleDislike(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return getNextProfile(msg, telegramClient)
    }

    private fun handleMenu(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.MENU)
        browsingQueue.remove(msg.userId)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)

        return listOf(createSendMessage(msg, "Главное меню", Keyboard.menu()))
    }

    private fun handleProfiles(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        userStateService.updateState(msg.userId, UserState.BROWSING_PROFILES)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return getNextProfile(msg, telegramClient)
    }

    private fun handleLike(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val likedUserId = msg.data.removePrefix("like_").toLong()
        val userProfile = profileService.getOrCreateProfile(msg.userId )

        val notification = matchService.createLikeNotification(userProfile, likedUserId)

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)

        return listOf(notification) + getNextProfile(msg, telegramClient)
    }

    private fun handleMatch(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val matchedUserId = msg.data.removePrefix("match_").toLong()

        MessageUtil.deleteMessage(msg.chatId, msg.messageId,telegramClient)
        return matchService.createMatchNotification(msg.userId, matchedUserId)
    }

    private fun getNextProfile(msg: MessageDto, telegramClient: TelegramClient): List<SendMessage> {
        val queue = browsingQueue[msg.userId] ?: mutableListOf()

        if (queue.isEmpty()) {
            // Загружаем еще анкет
            val userProfile = profileService.getOrCreateProfile(createUser(msg))
            loadProfilesForBrowsing(msg, userProfile)
        }

        val nextProfile = browsingQueue[msg.userId]?.removeFirstOrNull()

        return if (nextProfile != null) {
            listOf(createProfileMessage(msg, nextProfile))
        } else {
            listOf(createSendMessage(msg, "😔 Анкеты закончились. Попробуйте позже!"))
        }
    }

    private fun loadProfilesForBrowsing(msg: MessageDto, userProfile: Profile) {
        val matchingProfiles = profileService.findMatchingProfiles(userProfile)
        browsingQueue[msg.userId] = matchingProfiles.toMutableList()
    }
}