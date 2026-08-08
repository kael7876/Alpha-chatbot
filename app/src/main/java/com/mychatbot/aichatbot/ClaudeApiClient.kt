package com.mychatbot.aichatbot

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Client sederhana untuk memanggil Anthropic Messages API.
 * Setiap user memasukkan API key mereka sendiri (disimpan lokal di HP, tidak dikirim ke server manapun selain Anthropic).
 */
class ClaudeApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val endpoint = "https://api.anthropic.com/v1/messages"

    /**
     * Mengirim seluruh riwayat percakapan + system prompt ke Claude, mengembalikan balasan teks.
     * Melempar Exception dengan pesan yang bisa ditampilkan ke user jika gagal.
     */
    fun sendMessage(apiKey: String, systemPrompt: String, history: List<ChatMessage>): String {
        if (apiKey.isBlank()) throw IOException("API Key belum diisi.")

        val messagesArray = JSONArray()
        for (msg in history) {
            val obj = JSONObject()
            obj.put("role", msg.role)
            obj.put("content", msg.text)
            messagesArray.put(obj)
        }

        val body = JSONObject().apply {
            put("model", "claude-opus-4-1")
            put("max_tokens", 1024)
            put("system", systemPrompt)
            put("messages", messagesArray)
        }

        val request = Request.Builder()
            .url(endpoint)
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .post(body.toString().toRequestBody(mediaType))
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody).getJSONObject("error").getString("message")
                } catch (e: Exception) {
                    "Terjadi kesalahan (kode ${response.code})"
                }
                throw IOException(errorMsg)
            }

            val json = JSONObject(responseBody)
            val contentArray = json.getJSONArray("content")
            val textBuilder = StringBuilder()
            for (i in 0 until contentArray.length()) {
                val item = contentArray.getJSONObject(i)
                if (item.getString("type") == "text") {
                    textBuilder.append(item.getString("text"))
                }
            }
            return textBuilder.toString()
        }
    }
}
