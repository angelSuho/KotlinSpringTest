package com.learn.kopring.websocket.practice.presentation

data class BaseMessage(
    val sessionId: String,
    val command: String,
    val message: String? = null,
    val key: String? = null,
    val value: String? = null
)