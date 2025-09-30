package org.example.spacecore.bot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient


@Configuration
@EnableConfigurationProperties(TelegramProperties::class)
class BotConfig {
    @Bean
    fun telegramClient(telegramProperties: TelegramProperties) = OkHttpTelegramClient(telegramProperties.token)
}

@ConfigurationProperties(prefix = "telegram.bot")
data class TelegramProperties(
    val token: String
)