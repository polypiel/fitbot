package com.angelcalvo.fitbot

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy


class FitBot(
    token: String,
    private val creatorId: Int,
    private val googleCredentials: String
): AbilityBot(token, "fitbot") {

    override fun creatorId(): Int = creatorId

    fun log(): Ability =
        Ability
            .builder()
            .name("log")
            .info("Adds metrics")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.send("Hello world!", ctx.chatId()!!) }
            .build()

    fun chart(): Ability =
        Ability
            .builder()
            .name("chart")
            .info("Shows the chart")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.send(doChart(), ctx.chatId()!!)  }
            .build()

    fun current(): Ability =
        Ability
            .builder()
            .name("current")
            .info("Shows current measures of an user")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd(doCurrent(ctx.user().id), ctx.chatId()!!)  }
            .build()

    private fun doChart(): String {
        return GoogleSheetsHandler(googleCredentials).chart()
    }

    private fun doCurrent(userId: Int): String {
        return GoogleSheetsHandler(googleCredentials).current(userId)
    }
}