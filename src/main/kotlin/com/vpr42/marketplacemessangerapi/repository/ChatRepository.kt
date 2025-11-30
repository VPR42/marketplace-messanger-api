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
                .and(CHATS.STATUS.eq(ChatStatus.OPEN))
        )

    fun findChatmate(userId: UUID, chatId: Long): ChatmateInfo? {
        val chatmateIdField = DSL
            .case_()
            .`when`(CHATS.MASTER_ID.eq(userId), CHATS.CUSTOMER_ID)
            .otherwise(CHATS.MASTER_ID)
            .`as`("chatmate_id")

        return dsl
            .select(
                chatmateIdField,
                USERS.NAME,
                USERS.SURNAME,
                MASTERS_INFO.DESCRIPTION,
                JOBS.NAME.`as`("order_name")
            )
            .from(CHATS)
            .join(ORDERS)
            .on(ORDERS.ID.eq(CHATS.ORDER_ID))
            .join(JOBS)
            .on(JOBS.ID.eq(ORDERS.JOB_ID))
            .join(USERS)
            .on(USERS.ID.eq(chatmateIdField.cast(UUID::class.java)))
            .leftJoin(MASTERS_INFO)
            .on(MASTERS_INFO.MASTER_ID.eq(USERS.ID))
            .where(
                CHATS.MASTER_ID.eq(userId)
                    .or(CHATS.CUSTOMER_ID.eq(userId))
            )
            .and(CHATS.ORDER_ID.eq(chatId))
            .fetchOne { r ->
                ChatmateInfo(
                    chatmateId = r.get(chatmateIdField)
                        ?: throw IllegalArgumentException("chatmateId shouldn't be null"),
                    name = r.get(USERS.NAME) ?: throw IllegalArgumentException("name shouldn't be null"),
                    surname = r.get(USERS.SURNAME) ?: throw IllegalArgumentException("surname shouldn't be null"),
                    description = r.get(MASTERS_INFO.DESCRIPTION) ?: "Пользователь заказчик",
                    orderName = r.get(JOBS.NAME) ?: throw IllegalArgumentException("orderName shouldn't be null"),
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
