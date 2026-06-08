package com.happynovel.crawler

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/crawling")
class CrawlingAdminController(
    private val service: CrawlingPipelineService,
) {
    @GetMapping("/sites")
    fun sites(): List<SiteConfig> = service.siteConfigs()

    @PostMapping("/sites")
    fun createSite(@RequestBody request: CreateSiteConfigRequest): SiteConfig = service.createSiteConfig(request)

    @GetMapping("/book-sources")
    fun bookSources(): List<BookSource> = service.bookSources()

    @PostMapping("/book-sources")
    fun createBookSource(@RequestBody request: CreateBookSourceRequest): BookSource = service.createBookSource(request)

    @GetMapping("/tasks")
    fun tasks(): List<PipelineTask> = service.tasks()

    @PostMapping("/book-sources/{bookSourceId}/crawl")
    fun triggerBookCrawl(
        @PathVariable bookSourceId: String,
        @RequestBody request: TriggerCrawlRequest,
    ): PipelineTask = service.crawlBook(bookSourceId, request.html)

    @PostMapping("/tasks/{taskId}/retry")
    fun retryTask(
        @PathVariable taskId: String,
        @RequestBody request: TriggerCrawlRequest,
    ): PipelineTask = service.retryTask(taskId, request.html)

    @GetMapping("/book-sources/{bookSourceId}/raw-chapters")
    fun rawChapters(@PathVariable bookSourceId: String): List<RawChapterContent> = service.rawChapters(bookSourceId)
}
