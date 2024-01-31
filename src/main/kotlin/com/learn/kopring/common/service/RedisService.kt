package com.learn.kopring.common.service

import jakarta.annotation.PostConstruct
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RedisService(
    private val redisTemplate: RedisTemplate<String, Any>,
) {

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

}