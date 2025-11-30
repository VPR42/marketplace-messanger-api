package com.vpr42.marketplacemessangerapi.dto.response

import com.vpr42.marketplace.jooq.enums.ChatStatus

data class ChatResponse(
    val chatId: Long?,
    val status: ChatStatus,
)
