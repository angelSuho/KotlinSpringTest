package com.learn.kopring.common.service

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {

    private val hashOperations = redisTemplate.opsForHash<String, String>()

    @PostConstruct
    fun initialize() {
        redisTemplate.keys("*").forEach { key ->
            redisTemplate.delete(key)
        }
    }

    fun set(key: String, value: Any, expiration: Long, timeUnit: TimeUnit) {
        redisTemplate.opsForValue().set(key, value, expiration, timeUnit)
    }

    fun get(key: String): Any? {
        return redisTemplate.opsForValue().get(key)
    }

    fun delete(key: String) {
        redisTemplate.delete(key)
    }

    // 발표 세션의 모든 필드를 저장하는 메서드
    fun insertPresentation(presentationId: String, presentationData: Map<String, String>) {
        hashOperations.putAll("presentation:$presentationId", presentationData)
    }

    // 발표 세션의 특정 필드를 업데이트하는 메서드
    fun updatePresentationField(presentationId: String, field: String, value: String) {
        hashOperations.put("presentation:$presentationId", field, value)
    }

    // 발표 세션의 특정 필드 값을 가져오는 메서드
    fun getPresentationField(presentationId: String, field: String): String? {
        return hashOperations.get("presentation:$presentationId", field)
    }

    // 발표 세션의 모든 데이터를 가져오는 메서드
    fun getPresentationData(presentationId: String): Map<String, String> {
        return hashOperations.entries("presentation:$presentationId")
    }

    // 발표 세션의 모든 필드를 삭제하는 메서드
    fun deleteAllFields(presentationId: String) {
        hashOperations.keys("presentation:$presentationId").forEach { field ->
            hashOperations.delete("presentation:$presentationId", field)
        }
    }

    // 발표 세션의 특정 필드를 삭제하는 메서드
    fun deletePresentationField(presentationId: String, field: String) {
        hashOperations.delete("presentation:$presentationId", field)
    }
}
