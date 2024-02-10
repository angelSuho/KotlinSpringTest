package com.learn.kopring.websocket

import com.learn.kopring.common.config.CommonProperties
import com.learn.kopring.common.service.RedisService
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class PresentationPracticeService(
    private val redisService: RedisService,
    private val commonProperties: CommonProperties,
) {

    fun insertPresentation(qrCode: String, notificationStatus: Boolean) {
        redisService.updateHashField(qrCode, commonProperties.notificationStatus, notificationStatus.toString())
        redisService.updateHashField(qrCode, commonProperties.slideIndex, "1")
        redisService.updateHashField(qrCode, commonProperties.accumulatedPresentationTime, null.toString())
        startRecording(qrCode)
    }

    fun updatePresentationField(qrCode: String, field: String, value: String) {
        when (field) {
            commonProperties.notificationStatus -> {
                validateAndUpdateField(value, qrCode, commonProperties.notificationStatus)
            }
            commonProperties.slideIndex -> {
                redisService.updateHashField(qrCode, commonProperties.slideIndex, value)
            }
            commonProperties.recordCondition -> {
                validateAndUpdateField(value, qrCode, commonProperties.recordCondition)
            }
        }
    }

    private fun validateAndUpdateField(value: String, qrCode: String, field: String) {
        when (field) {
            commonProperties.recordCondition -> {
                when (value) {
                    "true" -> startRecording(qrCode)
                    "false" -> stopRecording(qrCode)
                    else -> throw IllegalArgumentException("Invalid value for $field: $value")
                }
            }
            commonProperties.notificationStatus -> {
                when (value) {
                    "true", "false" -> redisService.updateHashField(qrCode, field, value)
                    else -> throw IllegalArgumentException("Invalid value for $field: $value")
                }
            }
            else -> throw IllegalArgumentException("Invalid field: $field")
        }
    }

    fun deletePresentation(qrCode: String) {
        redisService.deleteAllFields(qrCode)
    }

    fun getPresentationStatus(qrCode: String, field: String): String? {
        return redisService.getHashField(qrCode, field) ?: throw IllegalArgumentException("No such field")
    }

    fun startRecording(qrCode: String) {
        redisService.updateHashField(qrCode, commonProperties.recentPresentationStartTime, Instant.now().toString())
    }

    fun stopRecording(qrCode: String) {
        val startTimeString = getPresentationStatus(qrCode, commonProperties.recentPresentationStartTime)
        startTimeString?.let { redisTimeString ->
            val startTime = Instant.parse(redisTimeString)
            val stopTime = Instant.now()
            val duration = Duration.between(startTime, stopTime)

            // 기존에 저장된 누적 시간 가져오기
            val accumulatedTimeString = getPresentationStatus(qrCode, commonProperties.accumulatedPresentationTime)
            val accumulatedTime = accumulatedTimeString?.let { Duration.parse(redisTimeString) } ?: Duration.ZERO

            // 새로운 누적 시간 계산
            val newAccumulatedTime = accumulatedTime.plus(duration)

            // 누적 시간 업데이트
            updatePresentationField(qrCode, commonProperties.accumulatedPresentationTime, newAccumulatedTime.toString())
        }
    }
}