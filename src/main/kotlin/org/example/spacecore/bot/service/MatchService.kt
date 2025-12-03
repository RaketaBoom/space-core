package org.example.spacecore.bot.service

import org.example.spacecore.bot.model.Profile
import org.example.spacecore.bot.util.createSendPhoto
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.generics.TelegramClient

@Service
class MatchService(
    private val profileService: ProfileService
) {

    fun sendLikeNotification(likerProfile: Profile, likedUserTelegramId: Long, telegramClient: TelegramClient){
        val messageText = """
            ❤️ Ваша анкета понравилась пользователю!
            
            Имя: ${likerProfile.name}
            Возраст: ${likerProfile.age}
            Описание: ${likerProfile.description}
        """.trimIndent()

        val keyboard = listOf(
            InlineKeyboardRow(
                InlineKeyboardButton("❤️ Взаимная симпатия").apply {
                    callbackData = "match_${likerProfile.id}"
                }
            )
        )
        telegramClient.execute(createSendPhoto(likedUserTelegramId, likerProfile.photoId,messageText,InlineKeyboardMarkup.builder().keyboard(keyboard).build()))

        val myMessageText = """
            ❤️ Ваша симпатия отправлена!
        """.trimIndent()
        val myMessage = SendMessage(likerProfile.telegramId.toString(), myMessageText)
        telegramClient.execute(myMessage)

    }

    fun createMatchNotification(user1Id: Long, user2Id: Long): List<SendMessage> {
        val user1Profile = profileService.getOrCreateProfile(
            User(user1Id, "User1", false).apply {
                firstName = "User1"
            }
        )

        val user2Profile = profileService.getOrCreateProfile(
            User(user2Id, "User2", false).apply {
                firstName = "User2"
            }
        )

        val messageToUser1 = """
            🎉 Взаимная симпатия!
            
            Вы понравились пользователю @${user2Profile.username ?: "пользователю"}!
            Хорошо вам пообщаться!
        """.trimIndent()

        val messageToUser2 = """
            🎉 Взаимная симпатия!
            
            Вы понравились пользователю @${user1Profile.username ?: "пользователю"}!
            Хорошо вам пообщаться!
        """.trimIndent()

        return listOf(
            SendMessage(user1Id.toString(), messageToUser1),
            SendMessage(user2Id.toString(), messageToUser2)
        )
    }
}