package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.tables.pojos.Messages
import com.vpr42.marketplace.jooq.tables.records.MessagesRecord
import com.vpr42.marketplace.jooq.tables.references.MESSAGES
import com.vpr42.marketplacemessangerapi.dto.Message
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.*

@Repository
class MessageRepository(
    private val dsl: DSLContext,
) {

    fun findLastMessage(chatId: UUID) = dsl
        .selectFrom(MESSAGES)
        .where(MESSAGES.CHAT_ID.eq(chatId))
        .orderBy(MESSAGES.SENT_AT.desc())
        .limit(1)
        .fetchOneInto(Messages::class.java)

    fun findMessagesPage(
        chatId: UUID,
        page: Int,
        pageSize: Int = 50
    ): List<Message> {
        val safePage = page.coerceAtLeast(0)
        val safeSize = pageSize.coerceIn(1, 100)
        val offset = safePage * safeSize

        return dsl
            .select(
                MESSAGES.CHAT_ID,
                MESSAGES.SENDER,
                MESSAGES.CONTENT,
                MESSAGES.SENT_AT
            )
            .from(MESSAGES)
            .where(MESSAGES.CHAT_ID.eq(chatId))
            .orderBy(MESSAGES.SENT_AT.desc()) // последние сообщения первыми
            .limit(safeSize)
            .offset(offset)
            .fetch { r ->
                Message(
                    chatId = requireNotNull(r.get(MESSAGES.CHAT_ID)) { "chatId shouldn't be null" },
                    sender = requireNotNull(
                        r.get(MESSAGES.SENDER, UUID::class.java)
                    ) { "sender shouldn't be null" },
                    content = requireNotNull(r.get(MESSAGES.CONTENT)) { "content shouldn't be null" },
                    sentAt = requireNotNull(
                        r.get(MESSAGES.SENT_AT, OffsetDateTime::class.java)
                    ) { "sentAt shouldn't be null" }
                )
            }
    }

    fun insert(message: MessagesRecord) = dsl
        .insertInto(MESSAGES)
        .set(message)
        .returning()
        .fetchOneInto(Messages::class.java)
}
