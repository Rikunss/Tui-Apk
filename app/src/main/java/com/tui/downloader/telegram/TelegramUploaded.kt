package com.tui.downloader.telegram

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import okhttp3.*
import java.io.File

/**
 * Uploader sederhana ke Telegram Bot API.
 * Mengirim file sebagai document.
 */
class TelegramUploader(private val context: Context) {

    private val client = OkHttpClient.Builder().build()

    private fun getBotToken(): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("bot_token", null)
    }

    private fun getChatId(): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("chat_id", null)
    }

    suspend fun upload(file: File) {
        val token = getBotToken()
        val chatId = getChatId()

        if (token.isNullOrEmpty() || chatId.isNullOrEmpty()) {
            Log.e("TG-UPLOADER", "Bot token atau chat id kosong!")
            return
        }

        val url = "https://api.telegram.org/bot$token/sendDocument"

        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("chat_id", chatId)
            .addFormDataPart(
                "document",
                file.name,
                RequestBody.create(MediaType.parse("application/octet-stream"), file)
            )
            .build()

        val req = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(req).execute().use { resp ->
                Log.d("TG-UPLOADER", "TG resp: ${resp.code()} - ${resp.message()}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
