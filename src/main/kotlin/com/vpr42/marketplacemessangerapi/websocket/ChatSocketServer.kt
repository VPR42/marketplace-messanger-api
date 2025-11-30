package com.vpr42.marketplacemessangerapi.websocket

import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Component
class ChatSocketServer(
    private val handler: ChatSocketHandler,
): WebSocketConfigurer {
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry
            .addHandler(handler, "/ws/chat")
            .setAllowedOriginPatterns("*")
    }
}
