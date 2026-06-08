package com.happynovel.crawler

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class CrawlingPipelineService(
    private val parser: NovelHtmlParser = NovelHtmlParser(),
    private val cleaner: ChapterCleaningService = ChapterCleaningService(QualityCheckService()),
) {
    private val siteConfigs = linkedMapOf<String, SiteConfig>()
    private val bookSources = linkedMapOf<String, BookSource>()
    private val rawChaptersBySource = linkedMapOf<String, MutableList<RawChapterContent>>()
    private val cleanChaptersBySource = linkedMapOf<String, MutableList<CleanChapterContent>>()
    private val tasks = mutableListOf<PipelineTask>()

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
        siteConfigs[site.id] = site
        return site
    }

    fun createBookSource(request: CreateBookSourceRequest): BookSource {
        require(siteConfigs.containsKey(request.siteConfigId)) { "siteConfigId not found" }
        val source = BookSource(
            siteConfigId = request.siteConfigId,
            bookTitle = request.bookTitle,
            sourceUrl = request.sourceUrl,
            updateIntervalMinutes = request.updateIntervalMinutes,
        )
        bookSources[source.id] = source
        return source
    }

    fun crawlBook(bookSourceId: String, html: String): PipelineTask {
        val source = bookSources.getValue(bookSourceId)
        val site = siteConfigs.getValue(source.siteConfigId)
        val links = parser.parseChapterLinks(html)
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
        )
        tasks += task
        return task
    }

    fun rawChapters(bookSourceId: String): List<RawChapterContent> = rawChaptersBySource[bookSourceId].orEmpty()

    fun cleanChapters(bookSourceId: String): List<CleanChapterContent> = cleanChaptersBySource[bookSourceId].orEmpty()

    fun siteConfigs(): List<SiteConfig> = siteConfigs.values.toList()

    fun bookSources(): List<BookSource> = bookSources.values.toList()

    fun tasks(): List<PipelineTask> = tasks.toList()
}

@Configuration
class CrawlingPipelineConfiguration {
    @Bean
    fun crawlingPipelineService(): CrawlingPipelineService = CrawlingPipelineService()
}
