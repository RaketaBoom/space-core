package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class FormText {
    companion object{

        fun start(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                listOf(
                    "Spacceee - чат бот для знакомств (сюда фото космоса)",
                    "Давайте создадим вашу анкету! Как вас зовут?"
                )
            )
        }

        fun age(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                "Сколько вам лет?")

        }
        fun errorAge(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                "Пожалуйста, введите корректный возраст (18-100)"
            )
        }

        fun gender(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg, "Выберите ваш пол:", Keyboard.genderKeyboard()))
        }

        fun lookingFor(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg,
                "Кого вы ищете?", Keyboard.lookingFor())
            )
        }

        fun description(msg: MessageDto): List<SendMessage>{
            return createMessages(msg, "Расскажите о себе:")
        }

        fun photo(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                "Отправьте ваше фото:"
            )
        }

        fun vibe(msg: MessageDto): List<SendMessage>{
            return listOf(createSendMessage(msg,
                "Выберите ваш вайб (0-9):", Keyboard.vibeKeyboard())
            )
        }
    }
}