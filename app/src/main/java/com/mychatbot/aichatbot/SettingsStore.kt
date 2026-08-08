package com.mychatbot.aichatbot

import android.content.Context
import android.content.SharedPreferences

/**
 * Menyimpan API key & system prompt di penyimpanan lokal HP masing-masing user.
 * Tidak pernah dikirim ke server manapun selain langsung ke Anthropic saat chat.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("ai_chatbot_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    var systemPrompt: String
        get() = prefs.getString("system_prompt", DEFAULT_PROMPT) ?: DEFAULT_PROMPT
        set(value) = prefs.edit().putString("system_prompt", value).apply()

    companion object {
        const val DEFAULT_PROMPT =
            "Kamu adalah Alpha, asisten AI yang gercep, santai, dan asik diajak ngobrol. Suruh apa aja langsung dikerjain dengan cepat dan jelas. Jawab dalam bahasa Indonesia gaya santai tapi tetap sopan."
    }
}
