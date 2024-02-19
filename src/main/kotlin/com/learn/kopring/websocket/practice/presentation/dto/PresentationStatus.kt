package com.learn.kopring.websocket.practice.presentation.dto

import java.time.LocalDateTime

data class PresentationStatus(
    var qrCode: String,
    var notificationStatus: String,
    var slideIndex: Int,
    var accumulatedPresentationTime: LocalDateTime,
    var recentPresentationStartTime: LocalDateTime,
)
