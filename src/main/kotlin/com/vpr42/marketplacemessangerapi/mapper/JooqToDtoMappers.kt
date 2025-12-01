package com.vpr42.marketplacemessangerapi.mapper

import com.vpr42.marketplace.jooq.tables.pojos.Chats
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse

fun Chats.toChatResponse() = ChatResponse(
    chatId = this.jobId,
)
