package com.vpr42.marketplacemessangerapi.service

import com.vpr42.marketplace.jooq.tables.records.ChatsRecord
import com.vpr42.marketplacemessangerapi.dto.ChatCart
import com.vpr42.marketplacemessangerapi.mappers.ChatToChatCartMapper
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.JobsRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class ChatManager(
    private val chatRepository: ChatRepository,
    private val jobsRepository: JobsRepository,
    private val chatCartMapper: ChatToChatCartMapper,
) {
    private val logger = LoggerFactory.getLogger(ChatManager::class.java)

    fun getChatsList(userId: UUID): List<ChatCart> {
        val chats = chatRepository.findAllByUserId(userId)
        return chats
            .map {
                chatCartMapper.parce(it, userId)
            }
            .filter { it.lastMessage != null }
            .sortedByDescending { it.lastMessage?.sentAt }
    }

    fun getChatmateInfo(chatId: UUID, userId: UUID) = requireNotNull(
        chatRepository.findChatmate(userId, chatId)
    ) {
        "Chatmate info not found"
    }

    fun createChat(customerId: UUID, jobId: UUID): ChatCart {
        val job = requireNotNull(jobsRepository.findById(jobId)) { "Job from request not found" }

        val chat = requireNotNull(
            chatRepository.insert(
                ChatsRecord(
                    id = UUID.randomUUID(),
                    jobId = jobId,
                    masterId = job.masterId,
                    customerId = customerId,
                )
            )
        ) {
            "Chat creating ends with error"
        }
        logger.info("Chat for order $jobId created successfully")

        return chatCartMapper.parce(chat, customerId)
    }

    fun getChatCart(userId: UUID, jobId: UUID): ChatCart {
        val chat = requireNotNull(chatRepository.findByJobIdAndUserId(userId, jobId)) {
            "Chat about this job is not exist or not supported"
        }

        return chatCartMapper.parce(chat, userId)
    }

    fun isChatExist(jobId: UUID, userId: UUID) = chatRepository.isChatExist(userId, jobId)

    fun isCanConnect(senderId: UUID, chatId: UUID): Boolean = chatRepository.isCanChat(senderId, chatId)
}
