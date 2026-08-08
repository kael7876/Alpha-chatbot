package com.mychatbot.aichatbot

data class ChatMessage(
    val role: String,   // "user" atau "assistant"
    val text: String
)
