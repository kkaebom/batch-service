package com.kkaebom.core.properties

import com.kkaebom.core.quertz.QuartzScheduleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("quartz.job")
class QuartzJobProperties(val volunteerCrawling: QuartzScheduleProperties) {
}