package com.vpr42.marketplacemessangerapi.dto.chat

data class ChatRequest(
    val senderId: String,
    val content: String,
)
