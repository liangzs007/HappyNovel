package com.happynovel.crawler

import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals

class CrawlingSchedulerTest {
    @Test
    fun `scheduler creates due latest crawl tasks using epoch minutes`() {
        val service = CrawlingPipelineService()
        val site = service.createSiteConfig(
            CreateSiteConfigRequest(
                name = "示例站点",
                baseDomain = "https://novels.example.com",
                rateLimitPerMinute = 30,
                maxConcurrency = 2,
                chapterListSelector = ".chapter-list a",
                chapterBodySelector = ".chapter-content",
                adBlocklist = emptyList(),
            )
        )
        val source = service.createBookSource(
            CreateBookSourceRequest(
                siteConfigId = site.id,
                bookTitle = "测试小说",
                sourceUrl = "https://novels.example.com/book/1",
                updateIntervalMinutes = 60,
            )
        )
        val scheduler = CrawlingScheduler(
            service = service,
            clock = Clock.fixed(Instant.ofEpochSecond(3_600), ZoneOffset.UTC),
        )

        scheduler.scheduleLatestCrawls()

        assertEquals(listOf(source.id), service.tasks().map { it.targetId })
        assertEquals(PipelineTaskType.CRAWL_LATEST, service.tasks().single().type)
    }
}
