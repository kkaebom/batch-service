package com.kkaebom.db.crawling.repository

import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawling
import org.springframework.data.jpa.repository.JpaRepository

interface VolunteerRecruitmentCrawlingRepository: JpaRepository<VolunteerRecruitmentCrawling, Long> {
    fun existsByUrl(url: String): Boolean
    fun findAllByUrlIn(url: List<String>): List<VolunteerRecruitmentCrawling>
}