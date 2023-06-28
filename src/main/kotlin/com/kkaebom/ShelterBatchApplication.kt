package com.kkaebom

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@EnableJpaAuditing
@EnableBatchProcessing
@SpringBootApplication
@ConfigurationPropertiesScan
class ShelterBatchApplication

fun main(args: Array<String>) {
    runApplication<ShelterBatchApplication>(*args)
}
