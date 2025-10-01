package org.example.spacecore.bot.handler

import org.example.spacecore.bot.callback.annotations.Callback
import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.model.UserState
import org.example.spacecore.bot.service.ProfileService
import org.example.spacecore.bot.service.UserStateService
import org.example.spacecore.bot.text.AdminText
import org.example.spacecore.bot.text.MenuText
import org.example.spacecore.bot.text.ReportText
import org.example.spacecore.bot.util.MessageUtil
import org.example.spacecore.bot.util.createProfileMessage
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
class ReportCallback(
    private val userStateService: UserStateService,
    private val profileService: ProfileService,
    private val callbackHandler: CallbackHandler
) {

    @Callback("report")
    private fun handleReport(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        userStateService.updateState(msg.userId, UserState.REPORT)
        MessageUtil.deleteMessage(msg.chatId, msg.messageId, telegramClient)

        return ReportText.report(msg)
    }

    @Callback("reportBlock_")
    private fun handleBlock(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        val blockedId = msg.data.removePrefix("reportBlock_").toLong()
        val blockedTelegramId = profileService.getTelegramId(blockedId)
        callbackHandler.getProfile(msg, telegramClient)

        val profile = profileService.getOrCreateProfile(blockedTelegramId)
        telegramClient.execute(createProfileMessage(callbackHandler.adminId, profile))

        return AdminText.blockProfileAdmin(msg, callbackHandler.adminId, profile.username, blockedId)
    }

    //Admin
    @Callback("replyReport_")
    private fun handleReplyReport(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        if (callbackHandler.isAdmin(msg.userId)) {
            val reportedId = msg.data.removePrefix("replyReport_").toLong()
            userStateService.updateStateAndData(
                callbackHandler.adminId,
                UserState.REPLY_REPORT,
                "reported_id",
                reportedId
            )

            return AdminText.replyReport(msg)
        } else
            return listOf()
    }

    @Callback("blockProfile_")
    private fun handleBlockProfile(msg: MessageDto, telegramClient: TelegramClient): List<BotApiMethodMessage> {
        if (callbackHandler.isAdmin(msg.userId)) {
            var blockedId = msg.data.removePrefix("blockProfile_").toLong()
            blockedId = profileService.getTelegramId(blockedId)

            profileService.updateActivityStatus(blockedId, false)
            userStateService.updateState(blockedId, UserState.DISABLED)

            userStateService.updateState(msg.userId, UserState.MENU)

            return ReportText.youBlocked(blockedId) + MenuText.menu(msg)
        } else
            return listOf()
    }
}