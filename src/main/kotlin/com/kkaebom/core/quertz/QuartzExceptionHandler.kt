package com.kkaebom.core.quertz

import org.springframework.stereotype.Component

@Component
class QuartzExceptionHandler {
    fun handleException(e: Exception) {
        e.printStackTrace()
    }
}