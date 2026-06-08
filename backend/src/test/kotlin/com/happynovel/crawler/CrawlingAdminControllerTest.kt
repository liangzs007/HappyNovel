package com.happynovel.crawler

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CrawlingAdminControllerTest {
    @Test
    fun `admin can create site source and trigger crawl task`() {
        val service = CrawlingPipelineService()
        val controller = CrawlingAdminController(service)

        val site = controller.createSite(defaultSiteRequest())
        val source = controller.createBookSource(defaultBookSource(site.id))
        val task = controller.triggerBookCrawl(source.id, TriggerCrawlRequest(sampleBookHtml()))

        assertEquals(listOf(site), controller.sites())
        assertEquals(listOf(source), controller.bookSources())
        assertEquals(listOf(task), controller.tasks())
        assertEquals(PipelineTaskStatus.SUCCEEDED, task.status)
        assertEquals(2, controller.rawChapters(source.id).size)
    }

    @Test
    fun `admin can retry failed crawl task`() {
        val service = CrawlingPipelineService()
        val controller = CrawlingAdminController(service)

        val site = controller.createSite(defaultSiteRequest())
        val source = controller.createBookSource(defaultBookSource(site.id))
        val failed = controller.triggerBookCrawl(source.id, TriggerCrawlRequest("<div class=\"chapter-list\"></div>"))
        val retried = controller.retryTask(failed.id, TriggerCrawlRequest(sampleBookHtml()))

        assertEquals(PipelineTaskStatus.FAILED, failed.status)
        assertEquals(PipelineTaskStatus.SUCCEEDED, retried.status)
        assertEquals(1, retried.retryCount)
    }

    private fun defaultSiteRequest(): CreateSiteConfigRequest = CreateSiteConfigRequest(
        name = "示例站点",
        baseDomain = "https://novels.example.com",
        rateLimitPerMinute = 30,
        maxConcurrency = 2,
        chapterListSelector = ".chapter-list a",
        chapterBodySelector = ".chapter-content",
        adBlocklist = listOf("请收藏本站", "最新网址"),
    )

    private fun defaultBookSource(siteId: String): CreateBookSourceRequest = CreateBookSourceRequest(
        siteConfigId = siteId,
        bookTitle = "测试小说",
        sourceUrl = "https://novels.example.com/book/1",
        updateIntervalMinutes = 360,
    )

    private fun sampleBookHtml(): String = """
        <div class="chapter-list">
          <a href="/book/1/1">第一章 青云宗</a>
          <a href="/book/1/2">第二章 入门试炼</a>
        </div>
    """.trimIndent()
}
