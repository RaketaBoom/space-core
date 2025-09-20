package org.example.spacecore.bot.keyboard

import org.example.spacecore.bot.model.Profile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow

class Keyboard {

    companion object {
        private val MENU: MutableList<String> = mutableListOf("🏠 Меню", "menu")
        private val PROFILES: MutableList<String> = mutableListOf("👥 Смотреть анкеты", "profiles")

        fun createInlineKeyboard(keyboards: MutableList<MutableList<MutableList<*>>>): InlineKeyboardMarkup {
            var keyboard: ArrayList<InlineKeyboardRow> = arrayListOf()

            for (row in keyboards)
            {
                var inlineKeyboardRow = InlineKeyboardRow()
                for (key in row) {
                    inlineKeyboardRow.add(
                        InlineKeyboardButton.builder()
                            .text(key[0] as String)
                            .callbackData(key[1] as String)
                            .build()
                    )
                }
                keyboard.add(inlineKeyboardRow)
            }
            return InlineKeyboardMarkup.builder().keyboard(keyboard).build()
        }
        

        fun lookingFor(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("👨 Мужчин", "looking_MALE"), mutableListOf("👩 Женщин", "looking_FEMALE")),
                    mutableListOf(mutableListOf("👥 Всех", "looking_OTHER"))
                )
            )
        }

        fun profile(profile: Profile): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("❤️", "like_${profile.telegramId}"), mutableListOf("👎","dislike_${profile.telegramId}")),
                    mutableListOf(MENU)
                )
            )
        }

        fun myProfile(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Редактировать", "edit"), mutableListOf("Сменить вайб", "change_vibe")),
                    mutableListOf(MENU),
                    mutableListOf(PROFILES)
                )
            )
        }

        fun menu(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(PROFILES),
                    mutableListOf(mutableListOf("Моя анкета", "my_profile"))
                )
            )
        }
    }
}