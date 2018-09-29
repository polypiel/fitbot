package com.angelcalvo.fitbot

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import java.nio.file.Path


class FitBot(
    token: String,
    private val creatorId: Int,
    private val googleCredentials: String
): AbilityBot(token, "fitbot") {

    override fun creatorId(): Int = creatorId

    fun log(): Ability =
        Ability.builder()
            .name("log")
            .info("Adds metrics")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd("Hello world!", ctx.chatId()!!) }
            .build()

    fun summary(): Ability =
        Ability.builder()
            .name("summary")
            .info("Shows summary info")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd(doSummary(), ctx.chatId()) }
            .build()

    fun summaryChart(): Ability =
        Ability.builder()
            .name("summary")
            .info("Shows summary info")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val img = doSummaryChart()
                val photo = SendPhoto().setCaption("Summary")
                    .setChatId(ctx.chatId())
                    .setPhoto(img.toFile())
                sender.sendPhoto(photo)
            }
            .build()

    fun current(): Ability =
        Ability.builder()
            .name("current")
            .info("Shows current measures of an user")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd(doCurrent(ctx.user().id), ctx.chatId())  }
            .build()


    private fun doCurrent(userId: Int): String {
        return GoogleSheetsHandler(googleCredentials).current(userId)
    }

    private fun doSummary(): String {
        return GoogleSheetsHandler(googleCredentials).summary()
    }

    // TODO delete img
    private fun doSummaryChart(): Path {
        val (names, values) = GoogleSheetsHandler(googleCredentials).summaryChart()
        return ChartHandler().summaryChart(names, values)
    }
}