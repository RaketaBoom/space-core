package org.example.spacecore.bot

import org.example.spacecore.bot.handler.CallbackHandler
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.BotSession
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer
import org.telegram.telegrambots.longpolling.starter.AfterBotRegistration
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.generics.TelegramClient
import org.example.spacecore.bot.handler.MessageHandler
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

@Component
class SpaceCoreBot(
private val messageHandler: MessageHandler,
private val callbackHandler: CallbackHandler
) : SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {
    private val telegramClient: TelegramClient


    init {
        telegramClient = OkHttpTelegramClient(getBotToken())
    }

    override fun getBotToken(): String {
        return "Token"
    }

    override fun getUpdatesConsumer(): LongPollingUpdateConsumer {
        return this
    }

    override fun consume(update: Update) {
        try {
            val responses = when {
                update.hasMessage() -> messageHandler.handleMessage(update.message, telegramClient)
                update.hasCallbackQuery() -> callbackHandler.handleCallback(update.callbackQuery, telegramClient)
                else -> emptyList()
            }

            responses.forEach { response ->
                telegramClient.execute(response)
            }
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
//        // We check if the update has a message and the message has text
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            // Set variables
//            val message_text = update.getMessage().getText()
//            val chat_id = update.getMessage().getChatId()
//
//            val message: SendMessage? = SendMessage // Create a message object
//                .builder()
//                .chatId(chat_id)
//                .text(message_text!!)
//                .build()
//            try {
//                telegramClient.execute<Message?, SendMessage?>(message) // Sending our message object to user
//            } catch (e: TelegramApiException) {
//                e.printStackTrace()
//            }
//        }
    }

    @AfterBotRegistration
    fun afterRegistration(botSession: BotSession) {
        println("Registered bot running state is: " + botSession.isRunning())
    }
}