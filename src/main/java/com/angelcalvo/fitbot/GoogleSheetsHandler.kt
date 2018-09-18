package com.angelcalvo.fitbot

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME


class GoogleSheetsHandler(private val credentialsJson: String) {

    fun current(userId: Int): String {
        if (!USER_MAPPING.containsKey(userId)) {
            return "User not recognized, please contact the admin"
        }

        val userName = USER_MAPPING[userId]
        val range = "$userName!A115:L116"

        val response = sheets().spreadsheets().values()
            .get(SHEET_ID, range)
            .execute()
        val values = response.getValues()
        return if (values == null || values.isEmpty()) {
            "No data found :("
        } else {
            "Current $userName's data (${values[1][0]}):\n---\n" +
                    (1..11).joinToString("\n") {
                        "${HEADERS[it]} | ${values[1].getOrNull(it) ?: ""} (${values[0].getOrNull(it) ?: ""})"
                    }
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
            .filter { it.first != null }
            .map { Pair(it.second, it.first.flatten()) }
        return "Summary\n---\n" +
                "| Fat | FFMI" +
                values.joinToString("\n") { "${it.first} | ${it.second[0]} | ${it.second[1]}" }
    }

    private fun sheets(): Sheets {
        val credentials: GoogleCredential = GoogleCredential
            .fromStream(credentialsJson.byteInputStream())
            .createScoped(SCOPES)

        val httpTransport: NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(httpTransport, JSON_FACTORY, credentials)
            .setApplicationName(APPLICATION_NAME)
            .build()
        return service
    }

    companion object {
        val JSON_FACTORY: JacksonFactory = JacksonFactory.getDefaultInstance()
        val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        const val SHEET_ID = "1Nb83F26KpRc-1T28_qgan7ryHe-2v-eWIGTRCWuj7iA"
        val HEADERS = listOf("Fecha", "Peso", "Cintura", "Hombro", "Bicep izq", "Bicep der", "Muslo", "Pantorrilla",
            "Pecho", "Cuello", "Grasa", "FFMI")
        //val UK_FFMI_ID = 1957195779
        //val ES_FFMI_ID = 1911419640
        val USER_MAPPING = mapOf(222426316 to "Angel", 224363059 to "Oliver", 14708999 to "Luis", 215774109 to "hg")

    }
}
