package com.learn.kopring.common.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "custom")
data class CommonProperties @ConstructorBinding constructor(
    val redis: RedisProperties,
) {

    data class RedisProperties(
        val host: String,
        val port: Int,
    )
}