package com.vpr42.marketplacemessangerapi.service

import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplacemessangerapi.dto.ChatCart
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.mapper.toChatResponse
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.JobsRepository
import com.vpr42.marketplacemessangerapi.repository.MessageRepository
import com.vpr42.marketplacemessangerapi.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.util.*

@Service
class ChatManager(
    private val chatRepository: ChatRepository,
    private val jobsRepository: JobsRepository,
    private val messageService: MessageService,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
) {
    private val logger = LoggerFactory.getLogger(ChatManager::class.java)

    fun getChatsList(userId: UUID): List<ChatCart> {
        val chats = chatRepository.findAllByUserId(userId)

        val lastMessages = chats.associate {
            val id = requireNotNull(it.jobId) { "Chat id shouldn't be null" }
            id to messageRepository.findLastMessage(id)
        }
        val chatmates = chats.associate {
            val id = requireNotNull(it.jobId) { "Chat id shouldn't be null" }
            id to userRepository.findById(
                if (it.masterId == userId) it.customerId else it.masterId
            )
        }

        return chats.map { chat ->
            val id = requireNotNull(chat.jobId) { "Chat id shouldn't be null" }
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

    fun getChatmateInfo(chatId: UUID, userId: UUID) = requireNotNull(
        chatRepository.findChatmate(userId, chatId)
    ) {
        "Chatmate info not found"
    }

    // TODO проверить и переписать функцию
    fun createChat(customerId: UUID, jobId: UUID): ChatResponse {
        val job = requireNotNull(jobsRepository.findById(jobId)) { "Job for this order not found" }
        require(!chatRepository.isChatExist(jobId)) { "Chat for this order already exist" }

        val chat = requireNotNull(
            chatRepository.insert(
                ChatsRecord(
                    jobId = jobId,
                    masterId = job.masterId,
                    customerId = customerId,
                )
            )
        ) {
            "Chat creating ends with error"
        }
        logger.info("Chat for order $jobId created successfully")

        messageService.saveMessage(
            Message(
                chatId = jobId,
                sender = customerId,
                content = "Добрый день. Можете выполнить заказ \"${job.name}\"?",
                sentAt = OffsetDateTime.now()
            )
        )

        return chat.toChatResponse()
    }

    fun isCanConnect(senderId: UUID, chatId: UUID): Boolean = chatRepository.isCanChat(senderId, chatId)
}
