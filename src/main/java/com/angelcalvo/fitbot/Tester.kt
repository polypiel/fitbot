package com.angelcalvo.fitbot

fun main(args: Array<String>) {
    val sheet = GoogleSheetsHandler(System.getenv("GOOGLE_CREDENTIALS"))

    ChartHandler().curerntChart(sheet.currentChart(222426316)!!)
}