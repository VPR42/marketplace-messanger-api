package com.vpr42.marketplacemessangerapi.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vpr42.marketplacemessangerapi.dto.Message
import com.vpr42.marketplacemessangerapi.dto.chat.ChatRequest
import com.vpr42.marketplacemessangerapi.service.ChatManager
import com.vpr42.marketplacemessangerapi.service.MessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.net.URLDecoder
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@Service
class ChatSocketHandler(
    private val messageService: MessageService,
    private val chatManager: ChatManager
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(ChatSocketHandler::class.java)

    private val mapper = jacksonObjectMapper()

    /** chatId -> sessions */
    private val sessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

    /** sessionId -> chatId */
    private val sessionToChat = ConcurrentHashMap<String, Long>()

    /** sessionId -> uid (who is this session) */
    private val sessionToUid = ConcurrentHashMap<String, UUID>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val parsed = parse(session) ?: run {
            session.close(CloseStatus.BAD_DATA.withReason("Incorrect query params"))
            return
        }

        val chatId = parsed.chatId
        val uid = parsed.uid

        if (uid == null) {
            logger.warn("Connection ${session.id} rejected: no uid in query")
            session.close(CloseStatus.POLICY_VIOLATION.withReason("uid is required"))
            return
        }

        // Проверка права подключиться
        val canConnect = try {
            chatManager.isCanConnect(uid, chatId)
        } catch (e: Exception) {
            logger.error("Failed to check isCanConnect for uid=$uid", e)
            false
        }

        if (!canConnect) {
            logger.warn("Connection ${session.id} rejected: uid=$uid cannot connect to chat=$chatId")
            session.close(CloseStatus.POLICY_VIOLATION.withReason("Access denied"))
            return
        }

        // Регистрируем сессию
        val set = sessions.computeIfAbsent(chatId) { CopyOnWriteArraySet() }
        set.add(session)

        sessionToChat[session.id] = chatId
        sessionToUid[session.id] = uid

        logger.info("Session ${session.id} connected to chat=$chatId, uid=$uid")
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        if (message !is TextMessage) return

        val chatId = sessionToChat[session.id]
            ?: run {
                session.close(CloseStatus.BAD_DATA.withReason("Session has no chat binding"))
                return
            }

        val request = runCatching {
            mapper.readValue<ChatRequest>(message.payload)
        }.getOrElse {
            logger.warn("Bad JSON from session ${session.id}: ${it.message}")
            session.close(CloseStatus.BAD_DATA.withReason("Bad JSON: ${it.message}"))
            return
        }

        val content = request.content.trim()
        if (content.isEmpty()) {
            // ничего не делаем с пустыми сообщениями
            return
        }

        val sender = runCatching { UUID.fromString(request.senderId) }.getOrElse {
            logger.warn("Bad senderId '${request.senderId}' from session ${session.id}")
            session.close(CloseStatus.BAD_DATA.withReason("Invalid senderId"))
            return
        }

        // Дополнительная защита: убедиться, что sender совпадает с тем, кто подключился в этой сессии
        val sessionUid = sessionToUid[session.id]
        if (sessionUid != sender) {
            logger.warn("Session ${session.id} senderId mismatch: sessionUid=$sessionUid, declaredSender=$sender")
            session.close(CloseStatus.POLICY_VIOLATION.withReason("senderId mismatch"))
            return
        }

        val msg = Message(
            chatId = chatId,
            sender = sender,
            content = content,
            sentAt = OffsetDateTime.now()
        )

        // Сохраняем (синхронно; при желании — сделать асинхронно)
        try {
            messageService.saveMessage(msg)
        } catch (e: Exception) {
            logger.error("Failed to save message for chat=$chatId, sender=$sender", e)
            // Можно не закрывать сессию — просто логируем и продолжаем.
        }

        broadcast(chatId, msg)
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val chatId = sessionToChat.remove(session.id)
        sessionToUid.remove(session.id)

        if (chatId == null) {
            logger.info("Closed session ${session.id} had no chat binding")
            return
        }

        sessions[chatId]?.let { set ->
            set.remove(session)
            if (set.isEmpty()) {
                sessions.remove(chatId)
                logger.info("Chat $chatId: no active sessions — cleaned up")
            }
        }

        logger.info("Session ${session.id} disconnected from chat=$chatId, status=$status")
    }

    private fun broadcast(chatId: Long, msg: Message) {
        val json = mapper.writeValueAsString(msg)
        val textMessage = TextMessage(json)

        sessions[chatId]?.forEach { s ->
            if (s.isOpen) {
                runCatching { s.sendMessage(textMessage) }
                    .onFailure {
                        logger.warn("Failed to send message to session ${s.id}: ${it.message}")
                    }
            }
        }
    }

    private fun parse(session: WebSocketSession): Parsed? {
        val uri = session.uri ?: return null
        val params = uri.rawQuery
            ?.split("&")
            ?.mapNotNull {
                val i = it.indexOf('=')
                if (i > 0) {
                    it.substring(0, i) to URLDecoder.decode(it.substring(i + 1), Charsets.UTF_8)
                } else {
                    null
                }
            }
            ?.toMap()
            .orEmpty()

        val chat = params["chat"]?.let {
            runCatching { it.toLong() }.getOrNull()
        } ?: return null

        val uid = params["uid"]?.let {
            runCatching { UUID.fromString(it) }.getOrNull()
        }

        return Parsed(chatId = chat, uid = uid)
    }

    private data class Parsed(
        val chatId: Long,
        val uid: UUID?,
    )
}
