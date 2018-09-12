package com.angelcalvo.fitbot

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.InputStream
import java.io.InputStreamReader
import org.glassfish.jersey.server.ServerProperties.APPLICATION_NAME
import com.google.api.services.sheets.v4.Sheets



class GoogleSheetsHandler {
    fun current(userId: Int): String {
        val HTTP_TRANSPORT: NetHttpTransport  = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()
        val userName = USER_MAPPING[userId]
        val range = "$userName!A115:L116"

        val response = service.spreadsheets().values()
            .get(SHEET_ID, range)
            .execute()
        val values = response.getValues()
        return if (values == null || values.isEmpty()) {
            "No data found :("
        } else {
            "Current $userName's data (${values[1][0]}):\n" +
            (1..11)
                .map {  "*${HEADERS[it]}*: ${values[1].getOrNull(it) ?: ""} (${values[0].getOrNull(it) ?: ""})" }
                .joinToString("\n" )
        }

    }

    fun chart(): String {
        val HTTP_TRANSPORT: NetHttpTransport  = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName(APPLICATION_NAME)
            .build()
        val range = "Oliver!A5:L5"

        val response = service.spreadsheets()
            .get(SHEET_ID)
            .execute()
        return "Not implemented yet"

    }

    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val inStream: InputStream  = GoogleSheetsHandler::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
        val clientSecrets: GoogleClientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(inStream))

        // Build flow and trigger user authorization request.
        val flow: GoogleAuthorizationCodeFlow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user");
    }

    companion object {
        const val CREDENTIALS_FILE_PATH = "/credentials.json"
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()
        val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        const val TOKENS_DIRECTORY_PATH = "tokens"
        const val SHEET_ID = "1Nb83F26KpRc-1T28_qgan7ryHe-2v-eWIGTRCWuj7iA"
        val HEADERS = listOf("Fecha", "Peso", "Cintura", "Hombro", "Bicep izq", "Bicep der", "Muslo", "Pantorrilla",
            "Pecho", "Cuello", "Grasa", "FFMI")
        val UK_FFMI_ID = 1957195779
        val ES_FFMI_ID = 1911419640
        val USER_MAPPING = mapOf(222426316 to "Angel", 224363059 to "Oliver", 14708999 to "Luis")

    }
}