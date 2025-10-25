package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.keyboard.Keyboard
import org.example.spacecore.bot.util.createSendMessage
import org.example.spacecore.bot.util.createSendPhoto
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto

class FormText {
    companion object{

        fun start(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                listOf(
                    "Spacceee - чат бот для знакомств",
                    "Давайте создадим вашу анкету! Как вас зовут?"
                )
            )
        }

        fun nameError(msg: MessageDto): List<SendMessage>{
            return createMessages(msg, "Пожалуйста, сократи текст до 50 символов")
        }

        fun age(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                "Сколько вам лет?")

        }
        fun ageError(msg: MessageDto): List<SendMessage>{
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

        fun descriptionError(msg: MessageDto): List<SendMessage>{
            return createMessages(msg, "Пожалуйста, сократи текст до 800 символов")
        }

        fun photo(msg: MessageDto, edit: Boolean = false): List<SendMessage>{
            return listOf(createSendMessage(msg,
                "Отправьте ваше фото:", if (edit) Keyboard.back() else null
            ))
        }

        fun vibe(msg: MessageDto): SendPhoto{
            return createSendPhoto(msg, "AgACAgIAAxkBAAMVaPytvUEEx7F7Meryy31bg_88HVcAAnr9MRu9duBLULLPS7p2rbYBAAMCAAN5AAM2BA",//"AgACAgIAAxkBAAIGb2jkHNCmDAj5rvSydWsyGF27iIHhAAJl9TEbzM0pS0PE-MWGTwmxAQADAgADeQADNgQ",
                    "Выберите ваш вайб (1-10):", Keyboard.vibeKeyboard())
        }

        //Редактирование профиля
        fun editProfile(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                listOf(
                    "Давайте создадим вашу анкету сначала! Как вас зовут?"
                )
            )
        }
        fun changeName(msg: MessageDto): List<SendMessage>{
            return createMessages(msg,
                listOf(
                    "Как вас зовут?"
                )
            )
        }
    }
}