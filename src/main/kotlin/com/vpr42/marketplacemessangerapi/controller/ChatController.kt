package com.vpr42.marketplacemessangerapi.controller

import com.vpr42.marketplacemessangerapi.dto.response.ChatResponse
import com.vpr42.marketplacemessangerapi.service.ChatManager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import java.util.UUID

@Controller
@RequestMapping("/api/chat")
@Tag(
    name = "Чаты",
    description = "Контроллер для менеджмента и работы с чатами"
)
class ChatController(
    private val chatManager: ChatManager
) {
    private val logger = LoggerFactory.getLogger(ChatController::class.java)

    @RequestMapping("/create/{id}")
    @Operation(description = "Метод создания чата к заказу")
    fun createChat(
        @RequestHeader("id") userId: String,
        @PathVariable("id") orderId: String
    ): ResponseEntity<ChatResponse?> {
        logger.info("Request to create chat for order $orderId")
        return ResponseEntity.ok(
            chatManager.createChat(UUID.fromString(userId), orderId.toLong())
        )
    }

    @RequestMapping("/close/{id}")
    @Operation(description = "Метод закрытия чата к заказу")
    fun closeChat(
        @PathVariable("id") orderId: String
    ): ResponseEntity<ChatResponse?> {
        logger.info("Request to close chat for order $orderId")
        return ResponseEntity.ok(
            chatManager.closeChat(orderId.toLong())
        )
    }
}
