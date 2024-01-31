package com.learn.kopring.common.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class JsonConvertor(
    private val objectMapper: ObjectMapper,
    ) {

    fun <T> readValue(json: String, valueType: Class<T>): T {
        return objectMapper.readValue(json, valueType)
    }
}
