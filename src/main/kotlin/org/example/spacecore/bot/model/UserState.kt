package org.example.spacecore.bot.model

enum class UserState {
    DISABLED,
    START,
    ENTERING_NAME,
    ENTERING_AGE,
    SELECTING_GENDER,
    SELECTING_LOOKING_FOR,
    ENTERING_DESCRIPTION,
    UPLOADING_PHOTO,
    SELECTING_VIBE,

    BROWSING_PROFILES,
    MY_PROFILE,
    VIEWING_MATCHES,
    MENU,

    REPORT,

    //Admin states
    REPLY_REPORT
}