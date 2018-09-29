package com.angelcalvo.fitbot

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME


data class SummaryData(val names: List<String>, val ffmiValues: List<Double>)
data class Measures(
    val weight: Double? = null,
    val neck: Double? = null,
    val shoulders: Double? = null,
    val breast: Double? = null,
    val biceps: Pair<Double?, Double?> = Pair(null, null),
    val waist: Double? = null,
    val thigh: Double? = null,
    val calf: Double? = null,
    val ffmi: Double? = null
) {
    fun values() =
        listOf(weight, neck, shoulders, breast, ((biceps.first ?: 0.0) + (biceps.second ?: 0.0)) / 2, waist,
        thigh, calf, ffmi)
}
data class CurrentData(
    val name: String,
    val date: String,
    val current: Measures,
    val diff: Measures
) {
    override fun toString(): String =
        "Current $name's data ($date):\n" +
                "*Neck*: ${current.neck}\n" +
                "*Shoulders*: ${current.shoulders}\n" +
                "*Breast*: ${current.breast}\n" +
                "*Biceps*: ${current.biceps.first} ${current.biceps.second}\n" +
                "*Waist*: ${current.waist}\n" +
                "*Thigh*: ${current.thigh}\n" +
                "*Calf*: ${current.calf}\n" +
                "---\n" +
                "*Weight*: ${current.weight}\n" +
                "---\n" +
                "*FFMI*: ${current.ffmi}"

    fun names() = listOf("Neck", "Shoulders", "Breast", "Biceps", "Waist", "Thigh", "Calf", "Weight", "FFMI")
}

class GoogleSheetsHandler(private val credentialsJson: String) {

    fun current(userId: Int): String {
        if (!USER_MAPPING.containsKey(userId)) {
            return ""
        }

        val userName = USER_MAPPING[userId]
        val range = "$userName!A115:L116"

        val response = sheets().spreadsheets().values()
            .get(SHEET_ID, range)
            .execute()
        val values = response.getValues()
        return if (values == null || values.isEmpty()) {
            ""
        } else {
            "Current $userName's data (${values[1][0]}):\n" +
                    (1..11).joinToString("\n") {
                        "*${HEADERS[it]}*: ${values[1].getOrNull(it) ?: ""} (${values[0].getOrNull(it) ?: ""})"
                    }
        }
    }

    fun currentChart(userId: Int): CurrentData {
        if (!USER_MAPPING.containsKey(userId)) {
            return CurrentData(userId.toString(), "?", Measures(), Measures())
        }

        val userName = USER_MAPPING[userId]
        val range = "$userName!A115:L116"

        val response = sheets().spreadsheets().values()
            .get(SHEET_ID, range)
            .execute()
        val values = response.getValues()
        return if (values == null || values.isEmpty()) {
            CurrentData(userId.toString(), "?", Measures(), Measures())
        } else {
            CurrentData(
                userName!!,
                values[1][0].toString(),
                Measures(
                    weight = cm(values[1].getOrNull(1)?.toString()),
                    neck = cm(values[1].getOrNull(9)?.toString()),
                    shoulders = cm(values[1].getOrNull(3)?.toString()),
                    breast = cm(values[1].getOrNull(8)?.toString()),
                    biceps = Pair(
                        cm(values[1].getOrNull(4)?.toString()),
                        cm(values[1].getOrNull(5)?.toString())),
                    waist = cm(values[1].getOrNull(2)?.toString()),
                    thigh = cm(values[1].getOrNull(6)?.toString()),
                    calf = cm(values[1].getOrNull(7)?.toString()),
                    ffmi = cm(values[1].getOrNull(11)?.toString())),
                Measures(
                    weight = cm(values[0].getOrNull(1)?.toString()),
                    neck = cm(values[0].getOrNull(9)?.toString()),
                    shoulders = cm(values[0].getOrNull(3)?.toString()),
                    breast = cm(values[0].getOrNull(8)?.toString()),
                    biceps = Pair(
                        cm(values[0].getOrNull(4)?.toString()),
                        cm(values[0].getOrNull(5)?.toString())),
                    waist = cm(values[0].getOrNull(2)?.toString()),
                    thigh = cm(values[0].getOrNull(6)?.toString()),
                    calf = cm(values[0].getOrNull(7)?.toString()),
                    ffmi = cm(values[0].getOrNull(11)?.toString()))
            )
        }
    }

    fun summary(): String {
        val response = sheets().spreadsheets().values()
            .batchGet(SHEET_ID)
            .setRanges(USER_MAPPING.values.map { "$it!K116:L116" })
            .execute()
        val values = response.valueRanges
            .map { it.getValues() }
            .zip(USER_MAPPING.values)
            .asSequence()
            .filter { it.first != null }
            .map { Pair(it.second, it.first.flatten()) }
            .toList()
        return "Summary:\n" +
                values.joinToString("\n") { "*${it.first}*: ${it.second[0]} fat, ${it.second[1]} FFMI" }

    }

    fun summaryChart(): SummaryData {
        val response = sheets().spreadsheets().values()
            .batchGet(SHEET_ID)
            .setRanges(USER_MAPPING.values.map { "$it!K116:L116" })
            .execute()
        val values = response.valueRanges
            .asSequence()
            .map { it.getValues()?.getOrNull(0)?.getOrNull(1) ?: "0" }
            .map { it.toString().toDouble() }
            .toList()

        return SummaryData(USER_MAPPING.values.toList(), values)
    }

    private fun sheets(): Sheets {
        val credentials: GoogleCredential = GoogleCredential
            .fromStream(credentialsJson.byteInputStream())
            .createScoped(SCOPES)

        val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        return Sheets.Builder(httpTransport, JSON_FACTORY, credentials)
            .setApplicationName(APPLICATION_NAME)
            .build()
    }

    private fun cm(value: String?): Double? =
        if (value != null) {
            val res = CM_PATTERN.matchEntire(value)
            if (res?.groupValues?.size == 2) {
                val (cm) = res.destructured
                cm.toDoubleOrNull()
            } else {
                null
            }
        } else {
            null
        }

    companion object {
        val JSON_FACTORY: JacksonFactory = JacksonFactory.getDefaultInstance()
        val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        const val SHEET_ID = "1Nb83F26KpRc-1T28_qgan7ryHe-2v-eWIGTRCWuj7iA"
        val HEADERS = listOf("Fecha", "Peso", "Cintura", "Hombro", "Bicep izq", "Bicep der", "Muslo", "Pantorrilla",
            "Pecho", "Cuello", "Grasa", "FFMI")
        val USER_MAPPING = mapOf(
            222426316 to "Angel",
            224363059 to "Oliver",
            14708999 to "Luis",
            215774109 to "hg",
            198764045 to "Aaron"
        )
        val CM_PATTERN =  """-?([\d.]+)(?:cm|kg|%)?""".toRegex()
    }
}
