package com.learn.kopring.websocket.practice.presentation

import com.learn.kopring.websocket.practice.domain.Constant.Companion.SESSION_ID_HEADER_NAME
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component

@Component
class HandshakeFilter(
): Filter {
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        if (request is HttpServletRequest && response is HttpServletResponse) {

            // Directly use account.practiceSessionId without extra variable
            response.addHeader(SESSION_ID_HEADER_NAME, "123")

            chain?.doFilter(request, response)

        } else {
            throw IllegalStateException("Non-HTTP request or response.")
        }
    }
}
