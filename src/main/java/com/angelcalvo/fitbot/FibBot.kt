package com.angelcalvo.fitbot

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import java.io.ByteArrayInputStream
import java.io.InputStream


class FitBot(
    token: String,
    private val creatorId: Int,
    private val googleCredentials: String
): AbilityBot(token, "fitbot") {

    override fun creatorId(): Int = creatorId

    @Deprecated("Use summary")
    fun summaryChart(): Ability =
        Ability.builder()
            .name("summary2")
            .info("Shows summary info")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd(doSummary(), ctx.chatId()) }
            .build()

    fun summary(): Ability =
        Ability.builder()
            .name("summary")
            .info("Plots summary info")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val (caption, bytes) = doSummaryChart()
                val photo = SendPhoto().setChatId(ctx.chatId()).setPhoto(ctx.user().userName, bytes)
                sender.sendPhoto(photo)
            }
            .build()

    @Deprecated("Use current")
    fun currentChart(): Ability =
        Ability.builder()
            .name("current2")
            .info("Shows current measures of an user")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx -> silent.sendMd(doCurrent(ctx.user().id), ctx.chatId())  }
            .build()

    fun current(): Ability =
        Ability.builder()
            .name("current")
            .info("Plots current measures of an user")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val (caption, img) = doCurrentChart(ctx.user().id)
                val photo = SendPhoto().setChatId(ctx.chatId()).setPhoto(ctx.user().userName, img)
                sender.sendPhoto(photo)
                silent.sendMd(caption, ctx.chatId())
            }
            .build()

    private fun doCurrent(userId: Int): String {
        return GoogleSheetsHandler(googleCredentials).current(userId)
    }

    private fun doCurrentChart(userId: Int): Pair<String, InputStream> {
        val data = GoogleSheetsHandler(googleCredentials).currentChart(userId)
        val bytes =  ChartHandler().curerntChart(data)
        return Pair(data.toString(), ByteArrayInputStream(bytes))
    }

    private fun doSummary(): String {
        return GoogleSheetsHandler(googleCredentials).summary()
    }

    private fun doSummaryChart(): Pair<String, InputStream> {
        val data = GoogleSheetsHandler(googleCredentials).summaryChart()
        val bytes =  ChartHandler().summaryChart(data)
        return Pair("Summary", ByteArrayInputStream(bytes))
    }
}