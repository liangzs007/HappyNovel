package com.happynovel.crawler

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.assertEquals

class CrawlingPipelineConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(CrawlingPipelineConfiguration::class.java)
        .withPropertyValues("app.crawling.repository-mode=SEED")

    @Test
    fun `seed mode provides in memory crawling service`() {
        contextRunner.run { context ->
            val service = context.getBean(CrawlingPipelineService::class.java)
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
                    updateIntervalMinutes = 360,
                )
            )

            assertEquals(listOf(source), service.bookSources())
        }
    }
}
