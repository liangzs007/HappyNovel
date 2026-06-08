package com.happynovel.crawler

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Instant

@Component
@ConditionalOnProperty(prefix = "app.crawling.scheduler", name = ["enabled"], havingValue = "true")
class CrawlingScheduler(
    private val service: CrawlingPipelineService,
    private val clock: Clock = Clock.systemUTC(),
) {
    @Scheduled(fixedDelayString = "\${app.crawling.scheduler.fixed-delay-ms:60000}")
    fun scheduleLatestCrawls() {
        service.scheduleLatestCrawls(Instant.now(clock).epochSecond / 60)
    }
}
