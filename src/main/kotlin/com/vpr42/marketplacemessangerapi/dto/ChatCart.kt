package com.vpr42.marketplacemessangerapi.dto

import java.util.UUID

data class ChatCart(
    val chatId: UUID,
    val chatmateName: String,
    val chatmateSurname: String,
    val chatmateAvatar: String,
    val lastMessage: Message?,
)
