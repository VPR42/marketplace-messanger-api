package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.tables.pojos.Messages
import com.vpr42.marketplace.jooq.tables.records.MessagesRecord
import com.vpr42.marketplace.jooq.tables.references.MESSAGES
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

@Repository
class MessageRepository(
    private val dsl: DSLContext,
) {

    fun insert(message: MessagesRecord) = dsl
        .insertInto(MESSAGES)
        .set(message)
        .returning()
        .fetchOneInto(Messages::class.java)
}
