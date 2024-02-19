package com.learn.kopring.websocket.practice.presentation

import com.learn.kopring.websocket.practice.service.MessageHandler
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.web.bind.annotation.RestController

@RestController
class PracticeWebSocketController(
    private val messageHandler: MessageHandler,
) {

    @MessageMapping("/practice/{sessionId}")
    @SendTo("/sub/practice/{sessionId}")
    fun enter(
        @Payload request: BaseMessage,
        headerAccessor: SimpMessageHeaderAccessor,
    ): BaseMessage {
        return messageHandler.handle(request)
    }

    @MessageMapping("/practice/{sessionId}/ping")
    @SendTo("/sub/practice/{sessionId}")
    fun healthCheck(
        @DestinationVariable sessionId: String,
        @Payload request: BaseMessage,
        headerAccessor: SimpMessageHeaderAccessor,
    ): BaseMessage {
        return BaseMessage(request.sessionId, "HEALTH_CHECK", "Pong!")
    }
}
