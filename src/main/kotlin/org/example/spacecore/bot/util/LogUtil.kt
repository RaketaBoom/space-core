package org.example.spacecore.bot.util

import org.example.spacecore.bot.dto.MessageDto
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class LogUtil {
    companion object{
        fun log(msg: MessageDto, answer: String) {
            println("\n ----------------------------")
            val dateFormat: DateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date: Date = Date()
            val text = if (msg.text != "") ("Text: " + msg.text) else ("Data: " + msg.data)
            println(dateFormat.format(date))
            println("Message from " + msg.firstName + " " + msg.lastName + ". (id = " + msg.userId + ") \n " + text)
            println("Bot answer: \n Function - " + answer)
        }
    }
}