package com.angelcalvo.fitbot

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.style.Styler
import java.nio.file.Files
import java.nio.file.Path


class ChartHandler {

    fun summaryChart(people: List<String>, ffmi: List<Double>): Path {
        // Create Chart
        val ffmiChart = CategoryChartBuilder()
            .width(400)
            .height(200)
            .theme(Styler.ChartTheme.Matlab)
            .build()
        // Customize Chart
        ffmiChart.styler.isLegendVisible = false
        ffmiChart.styler.setHasAnnotations(true)
        ffmiChart.styler.yAxisMax = 30.0
        // Series
        ffmiChart.addSeries("FFMI", people, ffmi)

        val temp = Files.createTempFile("fitbot", ".png")
        BitmapEncoder.saveBitmap(ffmiChart, temp.toAbsolutePath().toString(), BitmapEncoder.BitmapFormat.PNG)
        return temp
    }
}
