package com.learn.kopring.websocket.practice.service

import com.learn.kopring.websocket.practice.presentation.BaseMessage
import com.learn.kopring.websocket.practice.presentation.dto.UpdateActionRequest

class MessageHandlerImpl(
    private val practiceService: PracticeService,
): MessageHandler {

    override fun handle(message: BaseMessage): BaseMessage {
        return when (message.command) {
            "INSERT" -> handleInsert(message.sessionId, message)
            "UPDATE" -> handleUpdate(message.sessionId, message)
            "DELETE" -> handleDelete(message.sessionId)
            "GET" -> handleGet(message.sessionId, message)
            else -> BaseMessage(message.sessionId, "ERROR", "Invalid action")
        }
    }

    private fun handleInsert(sessionId: String, message: BaseMessage): BaseMessage {
        val notificationStatus = message.key.toBoolean()
        practiceService.insertPractice(sessionId, notificationStatus)
        return BaseMessage(sessionId, "INSERT", "Presentation inserted")
    }

    private fun handleUpdate(sessionId: String, message: BaseMessage): BaseMessage {
        val updateActionRequest = UpdateActionRequest(message.key!!, message.value!!)
        practiceService.updatePractice(sessionId, updateActionRequest)
        return BaseMessage(sessionId, "UPDATE", "Presentation updated", message.key, message.value)
    }

    private fun handleDelete(sessionId: String): BaseMessage {
        practiceService.deletePractice(sessionId)
        return BaseMessage(sessionId, "DELETE", "Presentation deleted")
    }

    private fun handleGet(sessionId: String, message: BaseMessage): BaseMessage {
        val value = practiceService.getPractice(sessionId, message.key!!)
        return BaseMessage(sessionId, "GET", value ?: "No such field")
    }
}
