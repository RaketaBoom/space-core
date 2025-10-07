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



        fun genderKeyboard(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("👨 Парень", "gender_MALE")),
                    mutableListOf(mutableListOf("👩 Девушка", "gender_FEMALE"))
                )
            )
        }

        fun vibeKeyboard(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("1", "vibe_1"),mutableListOf("2", "vibe_2"),mutableListOf("3", "vibe_3"), mutableListOf("4", "vibe_4"), mutableListOf("5", "vibe_5"),),
                    mutableListOf(mutableListOf("6", "vibe_6"),mutableListOf("7", "vibe_7"),mutableListOf("8", "vibe_8"),mutableListOf("9", "vibe_9"), mutableListOf("10", "vibe_10")),
                )
            )
        }

        fun back(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Назад", "myProfile")),
                )
            )
        }

        fun lookingFor(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("👨 Крутых парней", "lookingFor_MALE")),
                    mutableListOf(mutableListOf("👩 Прекрасных девушек", "lookingFor_FEMALE"))
                )
            )
        }

        fun profile(profile: Profile): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("❤️", "like_${profile.id}"), mutableListOf("👎","dislike_${profile.id}")),
                    mutableListOf(mutableListOf("Пожаловаться", "reportBlock_${profile.id}")),
                    mutableListOf(MENU)
                )
            )
        }

        fun myProfile(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("✏\uFE0F Редактировать", "editing")),
                    mutableListOf(MENU),
                    mutableListOf(PROFILES),
                    mutableListOf(mutableListOf("\uD83D\uDD12Отключить анкету", "profileInactive")),
                )
            )
        }

        fun editingProfile(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Заполнить сначала", "edit"), mutableListOf("Сменить вайб", "changeVibe")),
                    mutableListOf(mutableListOf("Имя", "changeName"), mutableListOf("Возраст", "changeAge")),
                    mutableListOf(mutableListOf("Фото", "changePhoto"), mutableListOf("Описание", "changeDescription")),
                    mutableListOf(MENU),
                    mutableListOf(PROFILES),
                    mutableListOf(mutableListOf("\uD83D\uDD12 Отключить анкету", "profileInactive")),
                )
            )
        }

        fun menu(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(PROFILES),
                    mutableListOf(mutableListOf("Моя анкета", "myProfile")),
                    mutableListOf(mutableListOf("Сообщить о проблеме", "report"))
                )
            )
        }

        fun formsEnded(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(PROFILES),
                    mutableListOf(MENU),
                    mutableListOf(mutableListOf("Моя анкета", "myProfile"))
                )
            )
        }

        fun openForm(profileId: Long): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Открыть анкету", "open_$profileId"))
                )
            )
        }

        fun report(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Ответить", "report"))
                )
            )
        }

        fun inactive(): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Включить анкету", "active"))
                )
            )
        }

        //Пожаловаться
        fun replyReport(userId: Long): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Ответить", "replyReport_$userId")),
                    mutableListOf(MENU),
                )
            )
        }

        fun blockProfile(userId: Long?): InlineKeyboardMarkup {
            return createInlineKeyboard(
                mutableListOf(
                    mutableListOf(mutableListOf("Заблокировать", "blockProfile_$userId")),
                    mutableListOf(MENU),
                )
            )
        }
    }
}