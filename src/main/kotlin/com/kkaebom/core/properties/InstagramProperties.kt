package com.kkaebom.core.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "instagram")
data class InstagramProperties(
    val loginId: String, val password: String, val channels: List<String>
)
