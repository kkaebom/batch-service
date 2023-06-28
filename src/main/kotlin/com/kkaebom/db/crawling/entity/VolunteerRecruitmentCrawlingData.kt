package com.kkaebom.db.crawling.entity

import jakarta.persistence.*

@Entity
class VolunteerRecruitmentCrawlingData(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "volunteer_recruitment_crawling_id")
    var volunteerRecruitmentCrawling: VolunteerRecruitmentCrawling? = null,
    var title: String? = null,
    var content: String,
) {

    fun addCrawling(crawling: VolunteerRecruitmentCrawling) {
        crawling.addData(this)
    }

    fun changeCrawling(crawling: VolunteerRecruitmentCrawling) {
        this.volunteerRecruitmentCrawling?.removeData(this)
        crawling.addData(this)
    }
}