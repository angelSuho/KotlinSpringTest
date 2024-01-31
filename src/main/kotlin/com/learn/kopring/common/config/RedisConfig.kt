package com.learn.kopring.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericToStringSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    private val commonProperties: CommonProperties,
) {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        RedisStandaloneConfiguration().let { config ->
            config.hostName = commonProperties.redis.host
            config.port = commonProperties.redis.port
            return LettuceConnectionFactory(config)
        }
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val redisTemplate = RedisTemplate<String, Any>()
        redisTemplate.connectionFactory = redisConnectionFactory()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = GenericToStringSerializer(Long::class.java)
        return redisTemplate
    }
}