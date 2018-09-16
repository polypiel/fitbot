package com.angelcalvo.fitbot

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

fun main(args: Array<String>) {
    val token = System.getenv("TG_TOKEN")
    val creatorId = System.getenv("TG_CREATOR_ID").toInt()
    val googleCredentials = System.getenv("GOOGLE_CREDENTIALS")

    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    try {
        telegramBotsApi.registerBot(FitBot(token, creatorId, googleCredentials))
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}
