package com.kkaebom.core.crawling

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime

@Configuration
class CrawlingConfig @Autowired constructor(
    val jobLauncher: JobLauncher,
    val shelterVolunteerCrawlingJob: Job,
) {

    @Bean
    fun crawlingChannel(): Channel<VolunteerRecruitmentCrawlingType> {
        return Channel()
    }

    @PostConstruct
    @OptIn(DelicateCoroutinesApi::class)
    fun init() {
        GlobalScope.launch {
            switchJob()
        }
    }

    suspend fun switchJob() {
        for (data in crawlingChannel()) {
            val jobParametersBuilder = JobParametersBuilder()
                .addLocalDateTime("jobStartTime", LocalDateTime.now())

            VolunteerRecruitmentCrawlingType.values().forEach {
                if (data == it) {
                    jobLauncher.run(
                        shelterVolunteerCrawlingJob,
                        jobParametersBuilder.addString("type", it.name).toJobParameters()
                    )
                }
            }
        }
    }

}