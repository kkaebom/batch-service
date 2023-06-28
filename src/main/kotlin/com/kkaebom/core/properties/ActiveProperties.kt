package com.kkaebom.core.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.profiles")
data class ActiveProperties(val active: String)