package com.learn.kopring.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.learn.kopring.common.service.RedisService
import org.springframework.stereotype.Component
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class PresentationWebSocketHandler(
    private val redisService: RedisService,
) : TextWebSocketHandler() {

    private val objectMapper = jacksonObjectMapper()

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val request: WebSocketRequest = objectMapper.readValue(message.payload)
        when(request.command) {
            "insertPresentation" -> redisService.insertPresentation(request.presentationId, request.data)
            "updateField" -> redisService.updatePresentationField(request.presentationId, request.data["field"]!!, request.data["value"]!!)
            "deleteField" -> redisService.deletePresentationField(request.presentationId, request.data["field"]!!)
            "deleteAllFields" -> redisService.deleteAllFields(request.presentationId)
        }

        val responsePayload = objectMapper.writeValueAsString(request)
        session.sendMessage(TextMessage(responsePayload))
    }
}

data class WebSocketRequest(
    val command: String,
    val presentationId: String,
    val data: Map<String, String>
)