package com.angelcalvo.fitbot

fun main(args: Array<String>) {
    val (names, values) = GoogleSheetsHandler(System.getenv("GOOGLE_CREDENTIALS")).summaryChart()
    val img = ChartHandler().summaryChart(names, values)
    println(img.toAbsolutePath().toString())
}