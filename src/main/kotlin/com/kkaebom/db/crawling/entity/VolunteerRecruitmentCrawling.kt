package com.kkaebom.db.crawling.entity

import jakarta.persistence.*

@Entity
class VolunteerRecruitmentCrawling(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "volunteerRecruitmentCrawling")
    var volunteerRecruitmentCrawlingDatas: MutableList<VolunteerRecruitmentCrawlingData> = mutableListOf(),
    var url: String,
    var site: String,
) {

    fun addData(data: VolunteerRecruitmentCrawlingData) {
        data.volunteerRecruitmentCrawling = this
        volunteerRecruitmentCrawlingDatas.add(data)
    }

    fun removeData(data: VolunteerRecruitmentCrawlingData) {
        volunteerRecruitmentCrawlingDatas.removeIf {
            it.id == data.id
        }
        data.volunteerRecruitmentCrawling = null
    }

}