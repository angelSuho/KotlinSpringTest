package com.learn.kopring.websocket.dto

data class UpdateActionRequest(
    val command: String,
    val value: String,
)