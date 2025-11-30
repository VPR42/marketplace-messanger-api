package com.vpr42.marketplacemessangerapi.controller

import com.vpr42.marketplacemessangerapi.dto.ChatCart
import com.vpr42.marketplacemessangerapi.dto.ChatmateInfo
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.service.ChatManager
import com.vpr42.marketplacemessangerapi.service.MessageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.*

@Controller
@RequestMapping("/api/chat")
@Tag(
    name = "Чаты",
    description = "Контроллер для менеджмента и работы с чатами"
)
class ChatController(
    private val chatManager: ChatManager,
    private val messageService: MessageService
) {
    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @PostMapping("/create/{id}")
    @Operation(summary = "Метод создания чата к заказу")
    fun createChat(
        @RequestHeader("id") userId: String,
        @PathVariable("id") orderId: String
    ): ResponseEntity<ChatResponse?> {
        logger.info("Request to create chat for order $orderId")
        return ResponseEntity.ok(
            chatManager.createChat(UUID.fromString(userId), orderId.toLong())
        )
    }

    @PatchMapping("/close/{id}")
    @Operation(summary = "Метод закрытия чата к заказу")
    fun closeChat(
        @PathVariable("id") orderId: String
    ): ResponseEntity<ChatResponse?> {
        logger.info("Request to close chat for order $orderId")
        return ResponseEntity.ok(
            chatManager.closeChat(orderId.toLong())
        )
    }

    @GetMapping("/all")
    @Operation(summary = "Метод получения всех чатов")
    fun getAllChatsList(
        @RequestHeader("id") userId: String,
    ): ResponseEntity<List<ChatCart>> {
        logger.info("Request to get all chats list of user $userId")
        return ResponseEntity.ok(
            chatManager.getChatsList(UUID.fromString(userId))
        )
    }

    @GetMapping("/open")
    @Operation(summary = "Метод получения только чатов с открытыми заказами")
    fun getOpenChatsList(
        @RequestHeader("id") userId: String,
    ): ResponseEntity<List<ChatCart>> {
        logger.info("Request to get open chats list of user $userId")
        return ResponseEntity.ok(
            chatManager.getChatsList(UUID.fromString(userId), true)
        )
    }

    @GetMapping("/{id}/chatmate")
    @Operation(summary = "Метод получения информации о собеседнике")
    fun getChatmateInfo(
        @PathVariable("id") chatId: String,
        @RequestHeader("id") userId: String,
    ): ResponseEntity<ChatmateInfo> {
        logger.info("Request get profile info for chat $chatId")
        return ResponseEntity.ok(
            chatManager.getChatmateInfo(chatId.toLong(), UUID.fromString(userId))
        )
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Метод получения последних \"X\" сообщений в чате")
    fun getChatHistory(
        @RequestHeader("id") userId: String,
        @PathVariable("id") chatId: String,
        @RequestParam("page") page: Int,
        @RequestParam("size") size: Int,
    ): ResponseEntity<List<Message>?> {
        logger.info("Request to get page $page of $size messages from $chatId chat")
        return ResponseEntity.ok(
            messageService.getMessagePage(
                userId = UUID.fromString(userId),
                chatId = chatId.toLong(),
                page = page,
                size = size
            )
        )
    }
}
