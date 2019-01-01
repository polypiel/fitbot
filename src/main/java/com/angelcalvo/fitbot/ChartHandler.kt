package com.angelcalvo.fitbot

import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.CategoryChartBuilder
import org.knowm.xchart.RadarChartBuilder
import org.knowm.xchart.style.Styler


class ChartHandler {

    fun summaryChart(data: SummaryData): ByteArray {
        // Create Chart
        val chart = CategoryChartBuilder()
            .width(WIDTH)
            .height(HEIGHT_2X)
            .theme(Styler.ChartTheme.Matlab)
            .title("FFMI")
            .build()
        // Customize Chart
        chart.styler.isLegendVisible = false
        chart.styler.setHasAnnotations(true)
        chart.styler.yAxisMax = 28.0
        chart.styler.yAxisMin = 16.0
        chart.styler.axisTickPadding = 0
        chart.styler.plotMargin = 0

        // Series
        chart.addSeries("FFMI", data.names, data.ffmiValues)

        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG)
    }

    fun curerntChart(data: CurrentData): ByteArray {
        val chart = RadarChartBuilder()
            .width(WIDTH)
            .height(HEIGHT)
            .theme(Styler.ChartTheme.Matlab)
            .title("${data.name} ${data.date}")
            .build()
        // Customize Chart
        chart.styler.isLegendVisible = false
        chart.styler.setHasAnnotations(true)
        chart.variableLabels = data.names().toTypedArray()
        chart.styler.markerSize = 0


        val values = data.diff.values().map { it ?: 0.0 }.map { Math.max(30 - it, 0.0) / 30.0 }
        chart.addSeries("current", values.toDoubleArray())

        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG)
    }

    companion object {
        const val WIDTH = 400
        const val HEIGHT = 200
        const val HEIGHT_2X = 400
    }
}
