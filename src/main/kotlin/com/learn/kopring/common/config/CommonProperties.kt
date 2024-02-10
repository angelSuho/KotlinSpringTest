package com.learn.kopring.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "custom")
data class CommonProperties @ConstructorBinding constructor(
    val redis: RedisProperties,
) {

    val notificationStatus = "notificationStatus"
    val slideIndex = "slideIndex"
    val accumulatedPresentationTime = "accumulatedPresentationTime" // 누적 발표 시간
    val recentPresentationStartTime = "recentPresentationStartTime" // 최근 발표 시작 시간
    val recordCondition = "recordCondition"

    data class RedisProperties(
        val host: String,
        val port: Int,
    )
}