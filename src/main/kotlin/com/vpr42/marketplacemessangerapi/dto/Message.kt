package com.vpr42.marketplacemessangerapi.dto

import java.time.OffsetDateTime
import java.util.UUID

data class Message(
    val chatId: UUID,
    val sender: UUID,
    val content: String,
    val sentAt: OffsetDateTime
)
