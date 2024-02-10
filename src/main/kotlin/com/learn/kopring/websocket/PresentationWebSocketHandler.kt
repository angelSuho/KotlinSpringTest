package com.learn.kopring.websocket

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.learn.kopring.common.util.LoggerProvider
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.springframework.web.util.UriComponentsBuilder
import java.util.concurrent.ConcurrentHashMap

@Component
class PresentationWebSocketHandler(
    private val presentationPracticeService: PresentationPracticeService,
) : TextWebSocketHandler() {

    private val log = LoggerProvider.getLogger<PresentationWebSocketHandler>()
    private val objectMapper = jacksonObjectMapper()
    private val userSessions = ConcurrentHashMap<String, WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val uri = session.uri // WebSocket 연결 URI를 가져옵니다.
        val qrCode = uri?.let {
            UriComponentsBuilder.fromUri(it).build().queryParams.getFirst("qrCode")
        }

        if (qrCode != null) {
            val existingSession = userSessions.putIfAbsent(qrCode, session)
            if (existingSession != null && existingSession != session) {
                existingSession.close(CloseStatus.SERVICE_RESTARTED)
            }
            session.sendMessage(TextMessage("Connection established with QR Code: $qrCode"))
        } else {
            session.close(CloseStatus.BAD_DATA)
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            val request = objectMapper.readValue(message.payload, WebSocketRequest::class.java)
            val qrCodeInSession = session.attributes["qrCode"] as? String

            if (qrCodeInSession != null && qrCodeInSession == request.qrCode) {
                when(request.command) {
                    "startPresentation" -> presentationPracticeService.insertPresentation(qrCodeInSession, request.data["notificationStatus"]!!.toBoolean())
                    "updateField" -> presentationPracticeService.updatePresentationField(qrCodeInSession, request.data["field"]!!, request.data["value"]!!)
                    "updateRecordCondition" -> presentationPracticeService.updatePresentationField(qrCodeInSession, "recordCondition", request.recordCondition)
                    "deleteAllFields" -> presentationPracticeService.deletePresentation(qrCodeInSession)
                }

                val responsePayload = objectMapper.writeValueAsString(request)
                session.sendMessage(TextMessage(responsePayload))
            } else {
                log.error("Invalid qrcode and Connection closed.")
                session.sendMessage(TextMessage("Invalid qrcode and Connection closed."))
                session.close(CloseStatus.BAD_DATA)
            }
        } catch (e: Exception) {
            log.error("json parse error or invalid command", e)
            session.sendMessage(TextMessage("json parse error or invalid command"))
        }
    }
}

data class WebSocketRequest(
    val command: String,
    val qrCode: String,
    val recordCondition: String,
    val notificationStatus: String,
    val data: Map<String, String>
)