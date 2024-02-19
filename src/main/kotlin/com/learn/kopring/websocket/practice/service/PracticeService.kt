package com.learn.kopring.websocket.practice.service

import com.learn.kopring.common.config.CommonProperties
import com.learn.kopring.common.service.RedisService
import com.learn.kopring.websocket.practice.presentation.dto.UpdateActionRequest
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class PracticeService(
    private val redisService: RedisService,
    private val commonProperties: CommonProperties,
) {

    fun insertPractice(sessionId: String, notificationStatus: Boolean) {
        redisService.updateHashField(sessionId, commonProperties.notificationStatus, notificationStatus.toString())
        redisService.updateHashField(sessionId, commonProperties.slideIndex, "1")
        redisService.updateHashField(sessionId, commonProperties.accumulatedPresentationTime, null.toString())
        startRecording(sessionId)
    }

    fun updatePractice(sessionId: String, request: UpdateActionRequest) {
        when (request.key) {
            commonProperties.notificationStatus -> {
                validateAndUpdateField(sessionId, commonProperties.notificationStatus, request.value)
            }
            commonProperties.slideIndex -> {
                redisService.updateHashField(sessionId, commonProperties.slideIndex, request.value)
            }
            commonProperties.recordCondition -> {
                validateAndUpdateField(sessionId, commonProperties.recordCondition, request.value)
            }
        }
    }

    fun deletePractice(sessionId: String) {
        redisService.deleteAllFields(sessionId)
    }

    fun getPractice(sessionId: String, field: String): String? {
        // 누적 시간 필드일 경우 누적 시간 계산후에 값을 가져옴
        if (field == commonProperties.accumulatedPresentationTime) {
            return calculateAccumulatedPresentationTime(sessionId)
        }
        return redisService.getHashField(sessionId, field) ?: throw IllegalArgumentException("No such field")
    }

    private fun validateAndUpdateField(sessionId: String, field: String, value: String) {
        when (field) {
            commonProperties.recordCondition -> {
                when (value) {
                    "true" -> startRecording(sessionId)
                    "false" -> stopRecording(sessionId, field, value)
                    else -> throw IllegalArgumentException("Invalid value for $field: $value")
                }
            }
            commonProperties.notificationStatus -> {
                when (value) {
                    "true", "false" -> redisService.updateHashField(sessionId, field, value)
                    else -> throw IllegalArgumentException("Invalid value for $field: $value")
                }
            }
            else -> throw IllegalArgumentException("Invalid field: $field")
        }
    }

    private fun startRecording(sessionId: String) {
        redisService.updateHashField(sessionId, commonProperties.recentPresentationStartTime, Instant.now().toString())
    }

    private fun stopRecording(sessionId: String, field: String, value: String) {
        calculateAccumulatedPresentationTime(sessionId)
        redisService.updateHashField(sessionId, field, value)
    }

    private fun calculateAccumulatedPresentationTime(sessionId: String): String {
        // 누적 시간 계산을 위한 변수
        var newAccumulatedTime: Duration = Duration.ZERO

        val startTimeString = getPractice(sessionId, commonProperties.recentPresentationStartTime)
        if (startTimeString != null) { // null 체크를 if로 변경
            val startTime = Instant.parse(startTimeString)
            val stopTime = Instant.now()
            val duration = Duration.between(startTime, stopTime)

            // 기존에 저장된 누적 시간 가져오기
            val accumulatedTimeString = getPractice(sessionId, commonProperties.accumulatedPresentationTime)
            val accumulatedTime = accumulatedTimeString?.let { Duration.parse(it) } ?: Duration.ZERO // 수정된 부분

            // 새로운 누적 시간 계산
            newAccumulatedTime = accumulatedTime.plus(duration)

            // 누적 시간 업데이트
            redisService.updateHashField(sessionId, commonProperties.accumulatedPresentationTime, newAccumulatedTime.toString())
        }
        // startTimeString이 null인 경우, 이미 초기화된 Duration.ZERO (또는 이전 값)을 반환합니다.
        return newAccumulatedTime.toString() // 누적 시간 반환
    }
}
