package com.kkaebom.db.crawling.repository

import com.kkaebom.db.crawling.entity.VolunteerRecruitmentCrawlingData
import org.springframework.data.jpa.repository.JpaRepository

interface VolunteerRecruitmentCrawlingDataRepository: JpaRepository<VolunteerRecruitmentCrawlingData, Long> {
}