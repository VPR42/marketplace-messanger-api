package com.vpr42.marketplacemessangerapi.dto

import java.util.UUID

data class ChatmateInfo(
    val chatmateId: UUID,
    val name: String,
    val surname: String,
    val avatar: String,
    val description: String,
    val orderName: String,
)
