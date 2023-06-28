package com.kkaebom.core.quertz

import com.kkaebom.core.quertz.QuartzBean
import org.quartz.*

interface QuartzJobService {

    fun init()

    fun buildJobTrigger(scheduleExp: String?): Trigger? {
        return TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp)).build()
    }

    fun buildJobDetail(job: Class<out QuartzBean>, name: String, group: String, param: Map<String, *>): JobDetail {
        val jobDataMap = JobDataMap()
        jobDataMap.putAll(param)
        return JobBuilder.newJob(job).withIdentity(name, group).usingJobData(jobDataMap).build()
    }

}