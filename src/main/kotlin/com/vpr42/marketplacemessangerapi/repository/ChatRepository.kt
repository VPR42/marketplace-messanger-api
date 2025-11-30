package com.vpr42.marketplacemessangerapi.repository

import com.vpr42.marketplace.jooq.enums.ChatStatus
import com.vpr42.marketplace.jooq.tables.pojos.Chats
import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplace.jooq.tables.references.CHATS
import com.vpr42.marketplace.jooq.tables.references.JOBS
import com.vpr42.marketplace.jooq.tables.references.MASTERS_INFO
import com.vpr42.marketplace.jooq.tables.references.ORDERS
import com.vpr42.marketplace.jooq.tables.references.USERS
import com.vpr42.marketplacemessangerapi.dto.ChatmateInfo
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ChatRepository(
    private val dsl: DSLContext
) {

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

    fun isCanConnect(userId: UUID, chatId: Long) = dsl
        .fetchExists(
            dsl.selectOne()
                .from(CHATS)
                .where(CHATS.ORDER_ID.eq(chatId))
                .and(
                    CHATS.MASTER_ID.eq(userId)
                        .or(CHATS.CUSTOMER_ID.eq(userId))
                )
                .and(CHATS.STATUS.eq(ChatStatus.OPEN))
        )

    fun findChatmate(userId: UUID, chatId: Long): ChatmateInfo? {
        // "сырое" выражение без алиаса — для join'а
        val chatmateExpr = DSL
            .case_()
            .`when`(CHATS.MASTER_ID.eq(userId), CHATS.CUSTOMER_ID)
            .otherwise(CHATS.MASTER_ID)

        // то же, но с алиасом — для select
        val chatmateIdField = chatmateExpr.`as`("chatmate_id")
        val orderNameField = JOBS.NAME.`as`("order_name")

        return dsl
            .select(
                chatmateIdField,
                USERS.NAME,
                USERS.SURNAME,
                USERS.AVATAR_PATH,
                MASTERS_INFO.DESCRIPTION,
                orderNameField
            )
            .from(CHATS)
            .join(ORDERS)
            .on(ORDERS.ID.eq(CHATS.ORDER_ID))
            .join(JOBS)
            .on(JOBS.ID.eq(ORDERS.JOB_ID))
            .join(USERS)
            .on(USERS.ID.eq(chatmateExpr)) // тут уже БЕЗ алиаса
            .leftJoin(MASTERS_INFO)
            .on(MASTERS_INFO.MASTER_ID.eq(USERS.ID))
            .where(
                CHATS.ORDER_ID.eq(chatId)
                    .and(
                        CHATS.MASTER_ID.eq(userId)
                            .or(CHATS.CUSTOMER_ID.eq(userId))
                    )
            )
            .fetchOne { r ->
                ChatmateInfo(
                    chatmateId = requireNotNull(r.get(chatmateIdField)) { "chatmateId shouldn't be null" },
                    name = requireNotNull(r.get(USERS.NAME)) { "name shouldn't be null" },
                    surname = requireNotNull(r.get(USERS.SURNAME)) { "surname shouldn't be null" },
                    avatar = requireNotNull(r.get(USERS.AVATAR_PATH)) { "avatar shouldn't be null" },
                    description = r.get(MASTERS_INFO.DESCRIPTION) ?: "Пользователь заказчик",
                    orderName = requireNotNull(r.get(orderNameField)) { "orderName shouldn't be null" },
                )
            }
    }

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
