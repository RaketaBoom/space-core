package org.example.spacecore.bot.text

import org.example.spacecore.bot.dto.MessageDto
import org.example.spacecore.bot.util.createSendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage


fun createMessages(msg: MessageDto, texts: Collection<String>): List<SendMessage>{
    val result = mutableListOf<SendMessage>()
    for (text in texts) {
        result.add(createSendMessage(msg, text))
    }
    return result
}
fun createMessages(msg: MessageDto, text: String): List<SendMessage> {
    return createMessages(msg, listOf(text))
}