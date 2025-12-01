package com.vpr42.marketplacemessangerapi.service

import com.vpr42.marketplace.jooq.tables.records.MessagesRecord
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.JobsRepository
import com.vpr42.marketplacemessangerapi.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class MessageService(
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,
    private val jobsRepository: JobsRepository
) {
    private val logger = LoggerFactory.getLogger(MessageService::class.java)

    fun createOrder(customerId: UUID, chatId: UUID): ChatResponse {
        require(!chatRepository.isChatExist(chatId)) { "Chat with id $chatId not exist" }
        val job = requireNotNull(jobsRepository.findById(chatId)) { "Job from chat $chatId not found" }

        saveMessage(
            Message(
                chatId = chatId,
                sender = customerId,
                content = "Добрый день. Можете выполнить заказ \"${job.name}\"?",
                sentAt = OffsetDateTime.now()
            )
        )

        return ChatResponse(chatId)
    }

    fun getMessagePage(
        userId: UUID,
        chatId: UUID,
        page: Int,
        size: Int
    ): List<Message> {
        require(chatRepository.isCanChat(userId, chatId)) {
            "The user $userId is not a member of $chatId chat"
        }

        return messageRepository.findMessagesPage(
            chatId = chatId,
            page = page,
            pageSize = size
        )
    }

    fun saveMessage(message: Message) {
        messageRepository.insert(
            MessagesRecord(
                id = UUID.randomUUID(),
                chatId = message.chatId,
                sender = message.sender,
                content = message.content,
                sentAt = message.sentAt
            )
        )
        logger.debug("Message in {} saved successfully", message.chatId)
    }
}
