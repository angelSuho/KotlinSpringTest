package com.learn.kopring.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    inline fun <reified T> getLogger(): Logger {
        return LoggerFactory.getLogger(T::class.java)
    }
}