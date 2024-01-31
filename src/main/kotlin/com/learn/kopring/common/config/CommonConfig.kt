package com.learn.kopring.common.config

import com.learn.kopring.common.infrastructure.config.CommonProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CommonProperties::class)
class CommonConfig