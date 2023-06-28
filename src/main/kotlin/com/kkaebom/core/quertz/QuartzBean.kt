package com.kkaebom.core.quertz

import org.quartz.JobExecutionContext
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.repository.JobRestartException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.quartz.QuartzJobBean
import org.springframework.stereotype.Component

@Component
abstract class QuartzBean : QuartzJobBean() {
    protected lateinit var quartzExceptionHandler: QuartzExceptionHandler
    protected lateinit var jobRepository: JobRepository
    protected lateinit var jobLauncher: JobLauncher

    @Autowired
    fun jobLauncher(jobLauncher: JobLauncher) {
        this.jobLauncher = jobLauncher
    }

    @Autowired
    fun jobRepository(jobRepository: JobRepository) {
        this.jobRepository = jobRepository
    }

    @Autowired
    fun exceptionHandler(quartzExceptionHandler: QuartzExceptionHandler) {
        this.quartzExceptionHandler = quartzExceptionHandler
    }

    override fun executeInternal(context: JobExecutionContext) {
        try {
            initJob(context)
        } catch (e: Exception) {
            quartzExceptionHandler.handleException(e)
        }
    }

    @Throws(JobInstanceAlreadyCompleteException::class, JobExecutionAlreadyRunningException::class, JobParametersInvalidException::class, JobRestartException::class)
    protected abstract fun initJob(context: JobExecutionContext)


}
