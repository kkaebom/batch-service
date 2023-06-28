package com.kkaebom.batch

import com.kkaebom.core.properties.QuartzJobProperties
import com.kkaebom.core.quertz.QuartzJobService
import jakarta.annotation.PostConstruct
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ShelterVolunteerCrawlingQuartzJobService(
    val scheduler: Scheduler,
    val properties: QuartzJobProperties
): QuartzJobService {
    val log: Logger = LoggerFactory.getLogger(ShelterVolunteerCrawlingQuartzJobService::class.java)

    @PostConstruct
    override fun init() {
        val job: JobDetail = buildJobDetail(
            ShelterVolunteerCrawlingQuartzJob::class.java,
            "shelterVolunteerCrawlingJob",
            "kkaebom",
            HashMap<String, Any>()
        )

        try {
            if (properties.volunteerCrawling.enable) {
//                val trigger = buildJobTrigger(properties.volunteerCrawling.schedule)
                scheduler.scheduleJob(job, TriggerBuilder.newTrigger().startNow().build())
            }
        } catch (e: Exception) {
            log.error("Shelter Volunteer Crawling Quartz Service!! {}", LocalDateTime.now())
        }
    }
}