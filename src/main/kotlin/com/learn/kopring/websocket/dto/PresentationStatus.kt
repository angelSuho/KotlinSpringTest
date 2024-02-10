package com.learn.kopring.websocket.dto

import java.time.LocalDateTime

data class PresentationStatus(
    var presentationId: String,
    var notificationStatus: String,
    var slideIndex: Int,
    var accumulatedPresentationTime: LocalDateTime,
    var recentPresentationStartTime: LocalDateTime,
)