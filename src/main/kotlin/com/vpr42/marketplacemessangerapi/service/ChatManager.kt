package com.vpr42.marketplacemessangerapi.service

import com.vpr42.marketplace.jooq.enums.ChatStatus
import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.mapper.toChatResponse
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.OrderRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatManager(
    private val chatRepository: ChatRepository,
    private val orderRepository: OrderRepository,
) {
    private val logger = LoggerFactory.getLogger(ChatManager::class.java)

    fun createChat(customerId: UUID, orderId: Long): ChatResponse {
        val job = requireNotNull(orderRepository.findJobByOrderId(orderId)) { "Job for this order not found" }
        require(chatRepository.isChatExist(job.userId, customerId)) { "Chat for this order already exist" }

        val chat = requireNotNull(
            chatRepository.insert(
                ChatsRecord(
                    orderId = orderId,
                    masterId = job.userId,
                    customerId = customerId,
                    status = ChatStatus.OPEN,
                )
            )
        ) {
            "Chat creating ends with error"
        }
        logger.info("Chat for order $orderId created successfully")

        return chat.toChatResponse()
    }

    fun closeChat(orderId: Long): ChatResponse {
        require(chatRepository.isChatExist(orderId)) { "Chat for this order already exist" }

        val chat = requireNotNull(chatRepository.closeChat(orderId)) { "Chat creating ends with error" }
        logger.info("Chat for order $orderId closed successfully")

        return chat.toChatResponse()
    }
}
