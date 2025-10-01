package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class ReportText {
    companion object{

        fun report(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg,
                "Напишите сообщение, которое будет отправлено администратору", Keyboard.back())
            )
        }

        fun reportEnded(msg: MessageDto): List<SendMessage>{
            return listOf(
                createSendMessage(msg, "Сообщение администратору отправлено!")
            )
        }

        fun youBlocked(userId: Long): List<SendMessage>{
            return listOf(
                createSendMessage(userId,
                    "На вашу анкету пожаловались, поэтому она была отключена!\nИсправьте анкету и вы сможете включить ее снова!\n\nЗа 3 жалобы анкета будет заблокирована.",
                    Keyboard.menu()
                )
            )
        }
    }
}