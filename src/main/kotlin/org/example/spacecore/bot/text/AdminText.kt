package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class AdminText {
    companion object {
        //Admin
        fun reportAdmin(msg: MessageDto, adminId: Long, text: String): List<SendMessage> {
            return listOf(
                createSendMessage(
                    adminId,
                    "Сообщение от пользователя @${msg.userName}\n$text", Keyboard.replyReport(msg.userId)
                )
            )
        }

        fun replyReport(msg: MessageDto): List<SendMessage> {
            return listOf(
                createSendMessage(
                    msg,
                    "Напишите сообщение, которое будет отправлено пользователю", Keyboard.back()
                )
            )
        }

        fun sendReportAdmin(reportedId: Long, text: String): List<SendMessage> {
            return listOf(
                createSendMessage(
                    reportedId,
                    "Сообщение от администратора: $text", Keyboard.report()
                )
            )
        }


        fun blockProfileAdmin(
            msg: MessageDto,
            adminId: Long,
            blockedUserName: String?,
            blockedUserId: Long?
        ): List<SendMessage> {
            return listOf(
                createSendMessage(
                    adminId,
                    "Пользователь @${msg.userName} отправил жалобу на профиль: @$blockedUserName",
                    Keyboard.blockProfile(blockedUserId)
                )
            )
        }

        fun newUserAdmin(msg: MessageDto, adminId: Long): SendMessage {
            return createSendMessage(
                adminId,
                "Пользователь @${msg.userName} запустил бота",
                disableNotification = true
            )
        }

        fun newProfileAdmin(msg: MessageDto, adminId: Long): SendMessage {
            return createSendMessage(
                adminId,
                "Пользователь @${msg.userName} создал анкету",
                disableNotification = true
            )
        }
    }
}