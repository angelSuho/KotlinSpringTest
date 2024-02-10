package com.learn.kopring.common.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.learn.kopring.websocket.dto.PresentationStatus
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
) {

    private val valueOperations: ValueOperations<String, Any> = redisTemplate.opsForValue()
    private val hashOperations = redisTemplate.opsForHash<String, String>()

    fun printAllValues() {
        val keys = redisTemplate.keys("*") ?: return
        keys.forEach { key ->
            val value = redisTemplate.opsForValue().get(key)
            println("Key: $key, Value: $value")
        }
    }

    /**
     * json 방식
     * */
    fun savePresentationStatus(status: PresentationStatus) {
        val jsonString = objectMapper.writeValueAsString(status)
        valueOperations.set(status.presentationId, jsonString)
    }

    fun getPresentationStatus(presentationId: String): PresentationStatus? {
        val jsonString = valueOperations.get(presentationId) ?: return null
        return objectMapper.readValue(jsonString.toString(), PresentationStatus::class.java)
    }

    fun updatePresentationFieldForJson(presentationId: String, field: String, value: String) {
        val status = getPresentationStatus(presentationId) ?: return
        when(field) {
            "notificationStatus" -> status.notificationStatus = value
            "slideIndex" -> status.slideIndex = value.toInt()
            "accumulatedPresentationTime" -> status.accumulatedPresentationTime = LocalDateTime.parse(value)
            "recentPresentationStartTime" -> status.recentPresentationStartTime = LocalDateTime.parse(value)
        }
        savePresentationStatus(status)
    }

    fun deletePresentationFieldForJson(presentationId: String) {
        redisTemplate.delete(presentationId)
    }

    /**
     * 해쉬 방식
     * */
    fun printHashTable(hashName: String) {
        println("Printing HashTable: $hashName")
        val keys = redisTemplate.opsForHash<String, String>().keys(hashName)
        keys.forEach { key ->
            val value = redisTemplate.opsForHash<String, String>().get(hashName, key)
            println("- HashKey: $key, Value: $value")
        }
    }

    // 발표 세션의 모든 필드를 저장하는 메서드
    fun insertHash(presentationId: String, presentationData: Map<String, String>) {
        hashOperations.putAll("presentation:$presentationId", presentationData)
    }

    // 발표 세션의 특정 필드를 업데이트하는 메서드
    fun updateHashField(presentationId: String, field: String, value: String) {
        hashOperations.put("presentation:$presentationId", field, value)
    }

    // 발표 세션의 특정 필드 값을 가져오는 메서드
    fun getHashField(presentationId: String, field: String): String? {
        return hashOperations.get("presentation:$presentationId", field)
    }

    // 발표 세션의 모든 데이터를 가져오는 메서드
    fun getHashTable(presentationId: String): Map<String, String> {
        return hashOperations.entries("presentation:$presentationId")
    }

    // 발표 세션의 모든 필드를 삭제하는 메서드
    fun deleteAllFields(presentationId: String) {
        hashOperations.keys("presentation:$presentationId").forEach { field ->
            hashOperations.delete("presentation:$presentationId", field)
        }
    }

    // 발표 세션의 특정 필드를 삭제하는 메서드
    fun deleteHashField(presentationId: String, field: String) {
        hashOperations.delete("presentation:$presentationId", field)
    }
}
