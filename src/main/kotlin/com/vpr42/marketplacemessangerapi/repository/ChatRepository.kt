package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.enums.ChatStatus
import com.vpr42.marketplace.jooq.tables.pojos.Chats
import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplace.jooq.tables.references.CHATS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ChatRepository(
    private val dsl: DSLContext
) {

    fun isChatExist(masterId: UUID, customerId: UUID) = dsl
        .fetchExists(
            dsl.selectOne()
                .from(CHATS)
                .where(CHATS.MASTER_ID.eq(masterId))
                .and(CHATS.CUSTOMER_ID.eq(customerId))
        )

    fun isChatExist(orderId: Long) = dsl
        .fetchExists(
            dsl.selectOne()
                .from(CHATS)
                .where(CHATS.ORDER_ID.eq(orderId))
        )

    fun isCanChat(userId: UUID, chatId: Long) = dsl
        .fetchExists(
            dsl.selectOne()
                .from(CHATS)
                .where(CHATS.ORDER_ID.eq(chatId))
                .and(
                    CHATS.MASTER_ID.eq(userId)
                        .or(CHATS.CUSTOMER_ID.eq(userId))
                )
        )

    fun findAllByUserId(userId: UUID): List<Chats> =
        dsl.selectFrom(CHATS)
            .where(
                CHATS.MASTER_ID.eq(userId)
                    .or(CHATS.CUSTOMER_ID.eq(userId))
            )
            .fetchInto(Chats::class.java)

    fun findOpenByUserId(userId: UUID) = dsl
        .selectFrom(CHATS)
        .where(
            CHATS.STATUS.eq(ChatStatus.OPEN)
                .and(
                    CHATS.MASTER_ID.eq(userId)
                        .or(CHATS.CUSTOMER_ID.eq(userId))
                )
        )
        .fetchInto(Chats::class.java)

    fun insert(record: ChatsRecord) = dsl
        .insertInto(CHATS)
        .set(record)
        .returning()
        .fetchOneInto(Chats::class.java)

    fun closeChat(orderId: Long) = dsl
        .update(CHATS)
        .set(CHATS.STATUS, ChatStatus.CLOSED)
        .where(CHATS.ORDER_ID.eq(orderId))
        .returning()
        .fetchOneInto(Chats::class.java)
}
