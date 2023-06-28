package com.kkaebom.batch

import com.kkaebom.core.crawling.VolunteerRecruitmentCrawlingType
import com.kkaebom.core.quertz.QuartzBean
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.quartz.JobExecutionContext
import org.springframework.batch.core.JobParametersInvalidException
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException
import org.springframework.batch.core.repository.JobRestartException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class ShelterVolunteerCrawlingQuartzJob: QuartzBean(){
    private lateinit var crawlingChannel: Channel<VolunteerRecruitmentCrawlingType>

    @Autowired
    fun crawlingChannel(crawlingChannel: Channel<VolunteerRecruitmentCrawlingType>) {
        this.crawlingChannel = crawlingChannel
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Throws(JobInstanceAlreadyCompleteException::class, JobExecutionAlreadyRunningException::class, JobParametersInvalidException::class, JobRestartException::class)
    override fun initJob(context: JobExecutionContext) {
        GlobalScope.launch {
            VolunteerRecruitmentCrawlingType.values().forEach {
                crawlingChannel.send(it)
            }
        }
    }


}