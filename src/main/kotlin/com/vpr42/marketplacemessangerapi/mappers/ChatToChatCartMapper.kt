package com.vpr42.marketplacemessangerapi.mappers

import com.vpr42.marketplace.jooq.tables.pojos.Chats
import com.vpr42.marketplacemessangerapi.dto.ChatCart
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.repository.ChatRepository
import com.vpr42.marketplacemessangerapi.repository.MessageRepository
import com.vpr42.marketplacemessangerapi.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class ChatToChatCartMapper(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
) {

    fun parce(chatId: UUID, userId: UUID) = parce(
        requireNotNull(chatRepository.findById(chatId)) { "Chat with id $chatId not found" },
        userId
    )

    fun parce(chat: Chats, userId: UUID): ChatCart {
        val id = requireNotNull(chat.id) { "Chat id shouldn't be null" }
        val chatmate = userRepository.findById(
            if (chat.masterId == userId) chat.customerId else chat.masterId
        )
        val lastMessage = messageRepository.findLastMessage(id)

        return ChatCart(
            chatId = id,
            chatmateName = chatmate?.name ?: "Имя",
            chatmateSurname = chatmate?.surname ?: "Фамилия",
            chatmateAvatar = chatmate?.avatarPath
                ?: "https://avatar.iran.liara.run/username?username=Фамилия+Имя",
            lastMessage = lastMessage?.let {
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
