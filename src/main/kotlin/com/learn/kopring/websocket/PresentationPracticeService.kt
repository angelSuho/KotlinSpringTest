package com.learn.kopring.websocket

import com.learn.kopring.common.config.CommonProperties
import com.learn.kopring.common.service.RedisService
import com.learn.kopring.websocket.dto.UpdateActionRequest
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

    fun updatePresentationField(qrCode: String, request: UpdateActionRequest) {
        when (request.command) {
            commonProperties.notificationStatus -> {
                validateAndUpdateField(qrCode, commonProperties.notificationStatus, request.value)
            }
            commonProperties.slideIndex -> {
                redisService.updateHashField(qrCode, commonProperties.slideIndex, request.value)
            }
            commonProperties.recordCondition -> {
                validateAndUpdateField(qrCode, commonProperties.recordCondition, request.value)
            }
        }
    }

    fun deletePresentation(qrCode: String) {
        redisService.deleteAllFields(qrCode)
    }

    fun getPresentationStatus(qrCode: String, field: String): String? {
        // 누적 시간 필드일 경우 누적 시간 계산후에 값을 가져옴
        if (field == commonProperties.accumulatedPresentationTime) {
            return calculateAccumulatedPresentationTime(qrCode)
        }
        return redisService.getHashField(qrCode, field) ?: throw IllegalArgumentException("No such field")
    }

    private fun validateAndUpdateField(qrCode: String, field: String, value: String) {
        when (field) {
            commonProperties.recordCondition -> {
                when (value) {
                    "true" -> startRecording(qrCode)
                    "false" -> stopRecording(qrCode, field, value)
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

    private fun startRecording(qrCode: String) {
        redisService.updateHashField(qrCode, commonProperties.recentPresentationStartTime, Instant.now().toString())
    }

    private fun stopRecording(qrCode: String, field: String, value: String) {
        calculateAccumulatedPresentationTime(qrCode)
        redisService.updateHashField(qrCode, field, value)
    }

    private fun calculateAccumulatedPresentationTime(qrCode: String): String {
        // 누적 시간 계산을 위한 변수
        var newAccumulatedTime: Duration = Duration.ZERO

        val startTimeString = getPresentationStatus(qrCode, commonProperties.recentPresentationStartTime)
        if (startTimeString != null) { // null 체크를 if로 변경
            val startTime = Instant.parse(startTimeString)
            val stopTime = Instant.now()
            val duration = Duration.between(startTime, stopTime)

            // 기존에 저장된 누적 시간 가져오기
            val accumulatedTimeString = getPresentationStatus(qrCode, commonProperties.accumulatedPresentationTime)
            val accumulatedTime = accumulatedTimeString?.let { Duration.parse(it) } ?: Duration.ZERO // 수정된 부분

            // 새로운 누적 시간 계산
            newAccumulatedTime = accumulatedTime.plus(duration)

            // 누적 시간 업데이트
            redisService.updateHashField(qrCode, commonProperties.accumulatedPresentationTime, newAccumulatedTime.toString())
        }
        // startTimeString이 null인 경우, 이미 초기화된 Duration.ZERO (또는 이전 값)을 반환합니다.
        return newAccumulatedTime.toString() // 누적 시간 반환
    }
}