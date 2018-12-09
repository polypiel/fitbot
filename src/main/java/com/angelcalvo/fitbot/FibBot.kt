package com.angelcalvo.fitbot

import org.telegram.abilitybots.api.bot.AbilityBot
import org.telegram.abilitybots.api.objects.Ability
import org.telegram.abilitybots.api.objects.Locality
import org.telegram.abilitybots.api.objects.Privacy
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import java.io.ByteArrayInputStream
import java.io.InputStream
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton


class FitBot(
    token: String,
    private val creatorId: Int,
    private val googleCredentials: String
): AbilityBot(token, "fitbot") {

    override fun creatorId(): Int = creatorId

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

    fun log(): Ability =
        Ability
            .builder()
            .name("log")
            .info("Adds measures (experimental)")
            .locality(Locality.ALL)
            .privacy(Privacy.PUBLIC)
            .action { ctx ->
                val sm = SendMessage(ctx.chatId()!!, log(ctx.arguments().toList(), ctx.user().id))
                val ikb = InlineKeyboardButton("Ok")
                ikb.callbackData = ctx.user().id.toString()
                val ikm = InlineKeyboardMarkup()
                ikm.keyboard = listOf(listOf(ikb))
                sm.replyMarkup = ikm
                sender.execute(sm)
            }
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


    private fun doCurrentChart(userId: Int): Pair<String, InputStream> {
        val data = GoogleSheetsHandler(googleCredentials).currentChart(userId)
        val bytes =  ChartHandler().curerntChart(data)
        return Pair(data.toString(), ByteArrayInputStream(bytes))
    }

    private fun doSummaryChart(): Pair<String, InputStream> {
        val data = GoogleSheetsHandler(googleCredentials).summaryChart()
        val bytes = ChartHandler().summaryChart(data)
        return Pair("Summary", ByteArrayInputStream(bytes))
    }

    private fun log(arguments: List<String>, userId: Int): String {
        return if (arguments.isEmpty()) {
            "You should add your weight. Example\n: \\log 80.5"
        } else {
            "Do you want to log ${arguments[0]} on 11/10"
        }

    }
}
