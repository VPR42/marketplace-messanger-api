package com.vpr42.marketplacemessangerapi.dto

import java.time.OffsetDateTime
import java.util.UUID

data class Message(
    val chatId: Long,
    val sender: UUID,
    val content: String,
    val sentAt: OffsetDateTime
)
