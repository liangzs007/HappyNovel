package com.happynovel.crawler

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrawlingPipelineTest {
    @Test
    fun `creates site config and book source`() {
        val service = CrawlingPipelineService()

        val site = service.createSiteConfig(
            CreateSiteConfigRequest(
                name = "示例站点",
                baseDomain = "https://novels.example.com",
                rateLimitPerMinute = 30,
                maxConcurrency = 2,
                chapterListSelector = ".chapter-list a",
                chapterBodySelector = ".chapter-content",
                adBlocklist = listOf("请收藏本站", "最新网址"),
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

        assertEquals(site.id, source.siteConfigId)
        assertEquals("测试小说", source.bookTitle)
        assertEquals(360, source.updateIntervalMinutes)
    }

    @Test
    fun `manual crawl creates succeeded task and stores raw chapters`() {
        val service = CrawlingPipelineService()
        val site = service.createSiteConfig(defaultSiteRequest())
        val source = service.createBookSource(defaultBookSource(site.id))

        val task = service.crawlBook(
            bookSourceId = source.id,
            html = sampleBookHtml(),
        )

        assertEquals(PipelineTaskStatus.SUCCEEDED, task.status)
        assertEquals(PipelineTaskType.CRAWL_BOOK, task.type)
        assertEquals(2, task.chaptersFound)
        assertTrue(service.rawChapters(source.id).first().rawContent.contains("第一章 青云宗"))
    }

    @Test
    fun `parser extracts chapter links and body content`() {
        val parser = NovelHtmlParser()

        val links = parser.parseChapterLinks(sampleBookHtml())
        val body = parser.parseChapterBody(sampleChapterHtml())

        assertEquals(listOf("/book/1/1", "/book/1/2"), links.map { it.url })
        assertEquals("第一章 青云宗", body.title)
        assertTrue(body.rawBody.contains("少年林辰踏入青云宗"))
    }

    @Test
    fun `cleaning removes html navigation ads and keeps paragraphs`() {
        val cleaner = ChapterCleaningService(QualityCheckService())

        val result = cleaner.clean(
            RawChapterContent(
                id = "chapter-1",
                bookSourceId = "source-1",
                title = "第1章 青云宗_最新网址",
                rawContent = sampleChapterHtml(),
            ),
            adBlocklist = listOf("请收藏本站", "最新网址"),
        )

        assertEquals("第1章 青云宗", result.title)
        assertEquals(CleanQualityStatus.PASSED, result.qualityStatus)
        assertEquals(listOf("少年林辰踏入青云宗。", "晨钟响彻山门。"), result.paragraphs)
    }

    @Test
    fun `quality check blocks short garbled or ad polluted chapters`() {
        val quality = QualityCheckService()

        val shortResult = quality.evaluate(title = "第一章", paragraphs = listOf("短"), adBlocklist = emptyList())
        val adResult = quality.evaluate(title = "第二章", paragraphs = listOf("请收藏本站 最新网址"), adBlocklist = listOf("请收藏本站"))

        assertEquals(CleanQualityStatus.BLOCKED, shortResult.status)
        assertEquals("正文过短", shortResult.reasons.single())
        assertEquals(CleanQualityStatus.NEEDS_REVIEW, adResult.status)
        assertEquals("疑似广告残留", adResult.reasons.single())
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
        <html>
          <body>
            <div class="chapter-list">
              <a href="/book/1/1">第一章 青云宗</a>
              <a href="/book/1/2">第二章 入门试炼</a>
            </div>
          </body>
        </html>
    """.trimIndent()

    private fun sampleChapterHtml(): String = """
        <html>
          <body>
            <h1>第一章 青云宗</h1>
            <div class="nav">上一章 下一章</div>
            <div class="chapter-content">
              <p>请收藏本站</p>
              <p>少年林辰踏入青云宗。</p>
              <p>晨钟响彻山门。</p>
              <script>alert('ad')</script>
              <p>最新网址</p>
            </div>
          </body>
        </html>
    """.trimIndent()
}
