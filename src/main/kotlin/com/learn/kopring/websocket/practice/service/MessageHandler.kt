package com.learn.kopring.websocket.practice.service

import com.learn.kopring.websocket.practice.presentation.BaseMessage

interface MessageHandler {
    fun handle(message: BaseMessage): BaseMessage
}