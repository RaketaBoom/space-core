package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class FormText {
    companion object{

        fun start(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg, "Spacceee - чат бот для знакомств (сюда фото космоса)"),
                createSendMessage(msg, "Давайте создадим вашу анкету! Как вас зовут?"))
        }

        fun name(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg, "Сколько вам лет?"))
        }
    }
}