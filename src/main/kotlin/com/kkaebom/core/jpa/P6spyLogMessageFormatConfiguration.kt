package com.kkaebom.core.jpa

import com.kkaebom.core.properties.ActiveProperties
import com.p6spy.engine.spy.P6SpyOptions
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class P6spyLogMessageFormatConfiguration(val activeProperties: ActiveProperties) {

    @PostConstruct
    fun setP6spyLogMessageFormat() {
        if (activeProperties.active == "prod") {
            return
        }
        P6SpyOptions.getActiveInstance().logMessageFormat = CustomP6spySqlFormat::class.java.name
    }
}