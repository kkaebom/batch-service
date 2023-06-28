package com.kkaebom.core.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack")
data class SlackProperties(
        val crawlingChannelId: String,
        val botToken: String,
        val signingSecret: String
)
