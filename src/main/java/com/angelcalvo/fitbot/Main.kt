package com.angelcalvo.fitbot

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import java.io.InputStream
import java.util.Properties

fun main(args: Array<String>) {
    val SECRETS_FILE = "/telegram.properties"
    val inStream: InputStream = GoogleSheetsHandler::class.java.getResourceAsStream(SECRETS_FILE)
    val props = Properties()
    props.load(inStream)
    inStream.close()
    val token = System.getenv("TG_TOKEN") ?: props["token"] as String
    val creatorId = (System.getenv("TG_CREATOR_ID") ?: props["creatorId"] as String).toInt()

    ApiContextInitializer.init()
    val telegramBotsApi = TelegramBotsApi()
    try {
        telegramBotsApi.registerBot(FitBot(token, creatorId))
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}
