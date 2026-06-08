package com.happynovel.crawler

import java.util.UUID

data class CreateSiteConfigRequest(
    val name: String,
    val baseDomain: String,
    val rateLimitPerMinute: Int,
    val maxConcurrency: Int,
    val chapterListSelector: String,
    val chapterBodySelector: String,
    val adBlocklist: List<String>,
)

data class SiteConfig(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val baseDomain: String,
    val rateLimitPerMinute: Int,
    val maxConcurrency: Int,
    val chapterListSelector: String,
    val chapterBodySelector: String,
    val adBlocklist: List<String>,
    val enabled: Boolean = true,
)

data class CreateBookSourceRequest(
    val siteConfigId: String,
    val bookTitle: String,
    val sourceUrl: String,
    val updateIntervalMinutes: Int,
)

data class BookSource(
    val id: String = UUID.randomUUID().toString(),
    val siteConfigId: String,
    val bookTitle: String,
    val sourceUrl: String,
    val updateIntervalMinutes: Int,
    val lastCheckedAtEpochMinutes: Long? = null,
)

data class ChapterLink(
    val title: String,
    val url: String,
)

data class ParsedChapterBody(
    val title: String,
    val rawBody: String,
)

data class RawChapterContent(
    val id: String,
    val bookSourceId: String,
    val title: String,
    val rawContent: String,
)

enum class CleanQualityStatus {
    PASSED,
    NEEDS_REVIEW,
    BLOCKED,
}

data class QualityCheckResult(
    val status: CleanQualityStatus,
    val reasons: List<String>,
)

data class CleanChapterContent(
    val rawChapterId: String,
    val title: String,
    val paragraphs: List<String>,
    val qualityStatus: CleanQualityStatus,
    val qualityReasons: List<String>,
)

enum class PipelineTaskType {
    CRAWL_BOOK,
    CRAWL_LATEST,
    CRAWL_CHAPTER,
    CLEAN_CHAPTER,
}

enum class PipelineTaskStatus {
    CREATED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED,
}

data class PipelineTask(
    val id: String = UUID.randomUUID().toString(),
    val type: PipelineTaskType,
    val status: PipelineTaskStatus,
    val targetId: String,
    val retryCount: Int = 0,
    val failureReason: String? = null,
    val chaptersFound: Int = 0,
)

data class TriggerCrawlRequest(
    val html: String,
)

data class ScheduleLatestCrawlsRequest(
    val nowEpochMinutes: Long,
)
