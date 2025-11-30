package com.vpr42.marketplacemessangerapi.dto

data class ChatCart(
    val chatId: Long,
    val chatmateName: String,
    val chatmateSurname: String,
    val chatmateAvatar: String,
    val lastMessage: Message?,
)
