package com.learn.kopring.common.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/pt").setAllowedOrigins("*").withSockJS() // 배포 후 cors 설정을 변경
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/practice")            // 서버가 클라이언트로 브로커 주소
        registry.setApplicationDestinationPrefixes("/client")  // 클라이언트가 서버로 메시지를 보낼 때 사용할 주소
    }
}