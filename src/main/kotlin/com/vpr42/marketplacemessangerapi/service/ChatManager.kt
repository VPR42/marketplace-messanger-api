package com.vpr42.marketplacemessangerapi.service

import com.vpr42.marketplace.jooq.enums.ChatStatus
import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplacemessangerapi.dto.ChatCart
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.mapper.toChatResponse
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.MessageRepository
import com.vpr42.marketplacemessangerapi.repository.OrderRepository
import com.vpr42.marketplacemessangerapi.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatManager(
    private val chatRepository: ChatRepository,
    private val orderRepository: OrderRepository,
    private val messageService: MessageService,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(ChatManager::class.java)

    fun getChatsList(userId: UUID, isOpen: Boolean = false): List<ChatCart> {
        val chats = if (isOpen) {
            chatRepository.findOpenByUserId(userId)
        } else {
            chatRepository.findAllByUserId(userId)
        }
        val lastMessages = chats.associate {
            val id = requireNotNull(it.orderId) { "Chat id shouldn't be null" }
            id to messageRepository.findLastMessage(id)
        }
        val chatmates = chats.associate {
            val id = requireNotNull(it.orderId) { "Chat id shouldn't be null" }
            id to userRepository.findById(
                if (it.masterId == userId) it.customerId else it.masterId
            )
        }

        return chats.map { chat ->
            val id = requireNotNull(chat.orderId) { "Chat id shouldn't be null" }
            ChatCart(
                chatId = id,
                chatmateName = chatmates[id]?.name ?: "Имя",
                chatmateSurname = chatmates[id]?.surname ?: "Фамилия",
                chatmateAvatar = chatmates[id]?.avatarPath
                    ?: "https://avatar.iran.liara.run/username?username=Фамилия+Имя",
                lastMessage = lastMessages[id]?.let {
                    Message(
                        chatId = id,
                        sender = it.sender,
                        content = it.content,
                        sentAt = it.sentAt,
                    )
                }
            )
        }
    }

    fun getChatmateInfo(chatId: Long, userId: UUID) = requireNotNull(
        chatRepository.findChatmate(userId, chatId)
    ) {
        "Chatmate info not found"
    }

    fun createChat(customerId: UUID, orderId: Long): ChatResponse {
        val job = requireNotNull(orderRepository.findJobByOrderId(orderId)) { "Job for this order not found" }
        require(!chatRepository.isChatExist(orderId)) { "Chat for this order already exist" }

        val chat = requireNotNull(
            chatRepository.insert(
                ChatsRecord(
                    orderId = orderId,
                    masterId = job.masterId,
                    customerId = customerId,
                    status = ChatStatus.OPEN,
                )
            )
        ) {
            "Chat creating ends with error"
        }
        logger.info("Chat for order $orderId created successfully")

        messageService.saveMessage(
            Message(
                chatId = orderId,
                sender = customerId,
                content = "Добрый день. Можете выполнить заказ \"${job.name}\"?",
                sentAt = OffsetDateTime.now()
            )
        )

        return chat.toChatResponse()
    }

    fun closeChat(orderId: Long): ChatResponse {
        require(chatRepository.isChatExist(orderId)) { "Chat for this order not exist" }

        val chat = requireNotNull(chatRepository.closeChat(orderId)) { "Chat creating ends with error" }
        logger.info("Chat for order $orderId closed successfully")

        return chat.toChatResponse()
    }

    fun isCanConnect(senderId: UUID, chatId: Long): Boolean = chatRepository.isCanConnect(senderId, chatId)
}
