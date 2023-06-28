package com.kkaebom.batch

import com.kkaebom.core.crawling.VolunteerRecruitmentCrawlingType
import com.kkaebom.core.crawling.WebCrawler
import com.kkaebom.core.slack.SlackChannel
import com.kkaebom.core.slack.SlackSender
import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawling
import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawlingData
import com.kkaebom.db.crawling.repository.VolunteerRecruitmentCrawlingDataRepository
import com.kkaebom.db.crawling.repository.VolunteerRecruitmentCrawlingRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class ShelterVolunteerCrawlingBatchConfig @Autowired constructor(
    val crawler: WebCrawler,
    val slackSender: SlackSender,
    val volunteerRecruitmentCrawlingRepository: VolunteerRecruitmentCrawlingRepository,
    val volunteerRecruitmentCrawlingDataRepository: VolunteerRecruitmentCrawlingDataRepository,
) {
    val log: Logger = LoggerFactory.getLogger(ShelterVolunteerCrawlingBatchConfig::class.java)

    @Bean
    fun shelterVolunteerCrawlingJob(
        jobRepository: JobRepository,
        shelterVolunteerCrawlingStep: Step
    ): Job? {
        log.info("shelterVolunteerCrawlingJob is start: ${LocalDateTime.now()}")
        return JobBuilder("shelterVolunteerCrawlingJob", jobRepository)
            .preventRestart()
            .start(shelterVolunteerCrawlingStep)
            .repository(jobRepository)
            .build()
    }

    @Bean
    fun shelterVolunteerCrawlingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        shelterVolunteerCrawlingReader: ItemReader<List<VolunteerRecruitmentCrawling>>,
        shelterVolunteerCrawlingWriter: ItemWriter<List<VolunteerRecruitmentCrawling>>
    ): Step {
        return StepBuilder("shelterVolunteerCrawlingStep", jobRepository)
            .chunk<List<VolunteerRecruitmentCrawling>, List<VolunteerRecruitmentCrawling>>(10, transactionManager)
            .reader(shelterVolunteerCrawlingReader)
            .processor(shelterVolunteerCrawlingProcessor())
            .writer(shelterVolunteerCrawlingWriter)
            .build()
    }

    @Bean
    @StepScope
    fun shelterVolunteerCrawlingReader(
        @Value("#{jobParameters['jobStartTime']}") jobStartTime: LocalDateTime,
        @Value("#{jobParameters['type']}") type: String,
    ): ItemReader<List<VolunteerRecruitmentCrawling>> {
        log.info("jobStartTime: $jobStartTime")
        var isRun = false

        return ItemReader {
            if (isRun) null
            else {
                isRun = true
                crawler.branchCrawling(VolunteerRecruitmentCrawlingType.valueOf(type))
            }
        }
    }

    @Bean
    fun shelterVolunteerCrawlingProcessor(): ItemProcessor<List<VolunteerRecruitmentCrawling>, List<VolunteerRecruitmentCrawling>> {
        return ItemProcessor {
            val alreadySaveUrls =
                volunteerRecruitmentCrawlingRepository.findAllByUrlIn(it.stream().map { it.url }.toList())
                    .associateBy { it.url }

            val saveCrawlings = mutableListOf<VolunteerRecruitmentCrawling>()

            it.forEach {
                if (!alreadySaveUrls.containsKey(it.url))
                    saveCrawlings.add(it)
            }
            saveCrawlings
        }
    }

    @Bean
    @StepScope
    fun shelterVolunteerCrawlingWriter(@Value("#{jobParameters['type']}") type: String): ItemWriter<List<VolunteerRecruitmentCrawling>> {
        return ItemWriter {
            var count = 0
            val stringBuilder = StringBuilder()
            it.forEach {
                count += it.size
                val crawlingDatas = mutableListOf<VolunteerRecruitmentCrawlingData>()
                it.forEach {
                    stringBuilder.append("\n${it.url}")
                    crawlingDatas.addAll(it.volunteerRecruitmentCrawlingDatas)
                }
                volunteerRecruitmentCrawlingRepository.saveAll(it)
                volunteerRecruitmentCrawlingDataRepository.saveAll(crawlingDatas)
            }
            if (count == 0) return@ItemWriter
            slackSender.sendSlackMessage("$type 에 새로운 글이 $count 개 있습니다.\n$stringBuilder", SlackChannel.CRAWLING)
        }
    }
}