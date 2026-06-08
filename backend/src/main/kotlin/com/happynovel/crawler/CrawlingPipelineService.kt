package com.happynovel.crawler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.beans.factory.ObjectProvider
import com.happynovel.content.ContentDatabaseClient
import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient

class CrawlingPipelineService(
    private val parser: NovelHtmlParser = NovelHtmlParser(),
    private val cleaner: ChapterCleaningService = ChapterCleaningService(QualityCheckService()),
    private val store: CrawlingStore = InMemoryCrawlingStore(),
) {
    private val rawChaptersBySource = linkedMapOf<String, MutableList<RawChapterContent>>()
    private val cleanChaptersBySource = linkedMapOf<String, MutableList<CleanChapterContent>>()

    fun createSiteConfig(request: CreateSiteConfigRequest): SiteConfig {
        require(request.rateLimitPerMinute > 0) { "rateLimitPerMinute must be positive" }
        require(request.maxConcurrency > 0) { "maxConcurrency must be positive" }

        val site = SiteConfig(
            name = request.name,
            baseDomain = request.baseDomain,
            rateLimitPerMinute = request.rateLimitPerMinute,
            maxConcurrency = request.maxConcurrency,
            chapterListSelector = request.chapterListSelector,
            chapterBodySelector = request.chapterBodySelector,
            adBlocklist = request.adBlocklist,
        )
        return store.saveSiteConfig(site)
    }

    fun createBookSource(request: CreateBookSourceRequest): BookSource {
        require(store.siteConfig(request.siteConfigId) != null) { "siteConfigId not found" }
        require(request.updateIntervalMinutes > 0) { "updateIntervalMinutes must be positive" }
        val source = BookSource(
            siteConfigId = request.siteConfigId,
            bookTitle = request.bookTitle,
            sourceUrl = request.sourceUrl,
            updateIntervalMinutes = request.updateIntervalMinutes,
        )
        return store.saveBookSource(source)
    }

    fun markBookSourceChecked(bookSourceId: String, checkedAtEpochMinutes: Long): BookSource =
        store.saveBookSourceChecked(bookSourceId, checkedAtEpochMinutes)

    fun scheduleLatestCrawls(nowEpochMinutes: Long): List<PipelineTask> {
        val dueSources = store.bookSources().filter { source ->
            val lastCheckedAt = source.lastCheckedAtEpochMinutes
            val isDue = lastCheckedAt == null || nowEpochMinutes - lastCheckedAt >= source.updateIntervalMinutes
            isDue && !hasOpenLatestCrawlTask(source.id)
        }
        return dueSources.map { source ->
            val task = PipelineTask(
                type = PipelineTaskType.CRAWL_LATEST,
                status = PipelineTaskStatus.CREATED,
                targetId = source.id,
            )
            store.saveTask(task)
        }
    }

    private fun hasOpenLatestCrawlTask(bookSourceId: String): Boolean =
        store.tasks().any {
            it.type == PipelineTaskType.CRAWL_LATEST &&
                it.targetId == bookSourceId &&
                it.status in setOf(PipelineTaskStatus.CREATED, PipelineTaskStatus.RUNNING)
        }

    fun crawlBook(bookSourceId: String, html: String): PipelineTask {
        return executeCrawlBook(
            bookSourceId = bookSourceId,
            html = html,
            retryCount = 0,
        )
    }

    fun retryTask(taskId: String, html: String): PipelineTask {
        val previous = store.tasks().first { it.id == taskId }
        require(previous.status == PipelineTaskStatus.FAILED) { "Only failed tasks can be retried" }
        return executeCrawlBook(
            bookSourceId = previous.targetId,
            html = html,
            retryCount = previous.retryCount + 1,
        )
    }

    private fun executeCrawlBook(bookSourceId: String, html: String, retryCount: Int): PipelineTask {
        val source = store.bookSource(bookSourceId) ?: throw NoSuchElementException("Book source not found: $bookSourceId")
        val site = store.siteConfig(source.siteConfigId) ?: throw NoSuchElementException("Site config not found: ${source.siteConfigId}")
        val links = parser.parseChapterLinks(html)
        if (links.isEmpty()) {
            val task = PipelineTask(
                type = PipelineTaskType.CRAWL_BOOK,
                status = PipelineTaskStatus.FAILED,
                targetId = bookSourceId,
                retryCount = retryCount,
                failureReason = "未解析到章节链接",
            )
            return store.saveTask(task)
        }

        val rawChapters = links.mapIndexed { index, link ->
            RawChapterContent(
                id = "${bookSourceId}-chapter-${index + 1}",
                bookSourceId = bookSourceId,
                title = link.title,
                rawContent = "<h1>${link.title}</h1><div class=\"chapter-content\"><p>${link.title}</p></div>",
            )
        }

        rawChaptersBySource[bookSourceId] = rawChapters.toMutableList()
        cleanChaptersBySource[bookSourceId] = rawChapters
            .map { cleaner.clean(it, site.adBlocklist) }
            .toMutableList()

        val task = PipelineTask(
            type = PipelineTaskType.CRAWL_BOOK,
            status = PipelineTaskStatus.SUCCEEDED,
            targetId = bookSourceId,
            chaptersFound = links.size,
            retryCount = retryCount,
        )
        return store.saveTask(task)
    }

    fun rawChapters(bookSourceId: String): List<RawChapterContent> = rawChaptersBySource[bookSourceId].orEmpty()

    fun cleanChapters(bookSourceId: String): List<CleanChapterContent> = cleanChaptersBySource[bookSourceId].orEmpty()

    fun siteConfigs(): List<SiteConfig> = store.siteConfigs()

    fun bookSources(): List<BookSource> = store.bookSources()

    fun tasks(): List<PipelineTask> = store.tasks()
}

@Configuration
class CrawlingPipelineConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun crawlingPipelineService(): CrawlingPipelineService = CrawlingPipelineService(store = crawlingStore())

    private fun crawlingStore(): CrawlingStore =
        when (environment.getProperty("app.crawling.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcCrawlingStore(databaseClient())
            else -> InMemoryCrawlingStore()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
