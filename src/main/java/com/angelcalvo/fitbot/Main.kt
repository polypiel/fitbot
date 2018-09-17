package com.angelcalvo.fitbot

import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import spark.Spark.get
import spark.Spark.port
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    thread {
        initBot()
    }

    println("Starting web server...")
    port(getHerokuAssignedPort())
    get("/") { req, res ->
        "Hello World"
    }
}

fun initBot() {
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

fun getHerokuAssignedPort(): Int {
    val processBuilder = ProcessBuilder()
    if (processBuilder.environment().get("PORT") != null) {
        return Integer.parseInt(processBuilder.environment().get("PORT"))
    }
    return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
}
