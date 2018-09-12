package com.angelcalvo.fitbot

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.InputStream
import java.util.Properties

fun main(args: Array<String>) {
    val token = System.getenv("TG_TOKEN")
    val creatorId = System.getenv("TG_CREATOR_ID").toInt()

    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    try {
        telegramBotsApi.registerBot(FitBot(token, creatorId))
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}
