package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class MenuText {
    companion object {

        fun menu(msg: MessageDto): List<SendMessage> {
            return listOf(createSendMessage(msg,
                "Главное меню", Keyboard.menu())
            )
        }

        fun formEnded(msg: MessageDto): List<SendMessage> {
            return listOf(createSendMessage(msg,
                "😔 Анкеты закончились. Попробуйте позже!", Keyboard.formsEnded())
            )
        }

        fun inactive(msg: MessageDto): List<SendMessage> {
            return listOf(createSendMessage(msg,
                "😔 Ваша анкета отключена.\nНо вы всегда можете ее включить!", Keyboard.inactive())
            )
        }

        fun active(msg: MessageDto): List<SendMessage> {
            return listOf(createSendMessage(msg,
                "С возвращением!\nГлавное меню:", Keyboard.menu())
            )
        }

    }
}