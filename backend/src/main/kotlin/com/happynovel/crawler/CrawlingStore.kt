package com.happynovel.crawler

import com.happynovel.admin.booleanValue
import com.happynovel.admin.intValue
import com.happynovel.admin.stringValue
import com.happynovel.content.ContentDatabaseClient
import java.util.UUID

interface CrawlingStore {
    fun saveSiteConfig(site: SiteConfig): SiteConfig
    fun saveBookSource(source: BookSource): BookSource
    fun saveTask(task: PipelineTask): PipelineTask
    fun saveBookSourceChecked(bookSourceId: String, checkedAtEpochMinutes: Long): BookSource
    fun siteConfig(id: String): SiteConfig?
    fun bookSource(id: String): BookSource?
    fun siteConfigs(): List<SiteConfig>
    fun bookSources(): List<BookSource>
    fun tasks(): List<PipelineTask>
}

class InMemoryCrawlingStore : CrawlingStore {
    private val sites = linkedMapOf<String, SiteConfig>()
    private val sources = linkedMapOf<String, BookSource>()
    private val tasks = mutableListOf<PipelineTask>()

    override fun saveSiteConfig(site: SiteConfig): SiteConfig {
        sites[site.id] = site
        return site
    }

    override fun saveBookSource(source: BookSource): BookSource {
        sources[source.id] = source
        return source
    }

    override fun saveTask(task: PipelineTask): PipelineTask {
        tasks += task
        return task
    }

    override fun saveBookSourceChecked(bookSourceId: String, checkedAtEpochMinutes: Long): BookSource {
        val updated = sources.getValue(bookSourceId).copy(lastCheckedAtEpochMinutes = checkedAtEpochMinutes)
        sources[bookSourceId] = updated
        return updated
    }

    override fun siteConfig(id: String): SiteConfig? = sites[id]

    override fun bookSource(id: String): BookSource? = sources[id]

    override fun siteConfigs(): List<SiteConfig> = sites.values.toList()

    override fun bookSources(): List<BookSource> = sources.values.toList()

    override fun tasks(): List<PipelineTask> = tasks.toList()
}

class JdbcCrawlingStore(
    private val databaseClient: ContentDatabaseClient,
) : CrawlingStore {
    override fun saveSiteConfig(site: SiteConfig): SiteConfig {
        databaseClient.update(
            """
                insert into site_config(
                    id,
                    name,
                    base_domain,
                    enabled,
                    rate_limit_per_minute,
                    max_concurrency,
                    chapter_list_selector,
                    chapter_body_selector,
                    ad_blocklist
                )
                values (?, ?, ?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    name = excluded.name,
                    base_domain = excluded.base_domain,
                    enabled = excluded.enabled,
                    rate_limit_per_minute = excluded.rate_limit_per_minute,
                    max_concurrency = excluded.max_concurrency,
                    chapter_list_selector = excluded.chapter_list_selector,
                    chapter_body_selector = excluded.chapter_body_selector,
                    ad_blocklist = excluded.ad_blocklist
            """.trimIndent(),
            UUID.fromString(site.id),
            site.name,
            site.baseDomain,
            site.enabled,
            site.rateLimitPerMinute,
            site.maxConcurrency,
            site.chapterListSelector,
            site.chapterBodySelector,
            site.adBlocklist.joinToString("\n"),
        )
        return site
    }

    override fun saveBookSource(source: BookSource): BookSource {
        databaseClient.update(
            """
                insert into book_source(
                    id,
                    book_id,
                    site_config_id,
                    book_title,
                    source_url,
                    update_interval_minutes,
                    last_checked_at_epoch_minutes
                )
                values (?, null, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    site_config_id = excluded.site_config_id,
                    book_title = excluded.book_title,
                    source_url = excluded.source_url,
                    update_interval_minutes = excluded.update_interval_minutes,
                    last_checked_at_epoch_minutes = excluded.last_checked_at_epoch_minutes
            """.trimIndent(),
            UUID.fromString(source.id),
            UUID.fromString(source.siteConfigId),
            source.bookTitle,
            source.sourceUrl,
            source.updateIntervalMinutes,
            source.lastCheckedAtEpochMinutes,
        )
        return source
    }

    override fun saveTask(task: PipelineTask): PipelineTask {
        databaseClient.update(
            """
                insert into pipeline_task(
                    id,
                    task_type,
                    status,
                    target_type,
                    target_id,
                    retry_count,
                    failure_reason,
                    payload
                )
                values (?, ?, ?, ?, ?, ?, ?, ?::jsonb)
                on conflict (id) do update set
                    status = excluded.status,
                    retry_count = excluded.retry_count,
                    failure_reason = excluded.failure_reason,
                    payload = excluded.payload
            """.trimIndent(),
            UUID.fromString(task.id),
            task.type.name,
            task.status.name,
            "BOOK_SOURCE",
            task.targetId,
            task.retryCount,
            task.failureReason,
            """{"chaptersFound":${task.chaptersFound}}""",
        )
        return task
    }

    override fun saveBookSourceChecked(bookSourceId: String, checkedAtEpochMinutes: Long): BookSource {
        databaseClient.update(
            """
                update book_source
                set last_checked_at_epoch_minutes = ?
                where id = ?
            """.trimIndent(),
            checkedAtEpochMinutes,
            UUID.fromString(bookSourceId),
        )
        return bookSource(bookSourceId) ?: throw NoSuchElementException("Book source not found: $bookSourceId")
    }

    override fun siteConfig(id: String): SiteConfig? =
        databaseClient.query("$SITE_SQL where id = ?", UUID.fromString(id)).firstOrNull()?.let(::mapSite)

    override fun bookSource(id: String): BookSource? =
        databaseClient.query("$SOURCE_SQL where id = ?", UUID.fromString(id)).firstOrNull()?.let(::mapSource)

    override fun siteConfigs(): List<SiteConfig> =
        databaseClient.query("$SITE_SQL order by created_at, name").map(::mapSite)

    override fun bookSources(): List<BookSource> =
        databaseClient.query("$SOURCE_SQL order by created_at, book_title").map(::mapSource)

    override fun tasks(): List<PipelineTask> =
        databaseClient.query("$TASK_SQL order by created_at").map(::mapTask)

    private fun mapSite(row: Map<String, Any?>): SiteConfig = SiteConfig(
        id = row.stringValue("id"),
        name = row.stringValue("name"),
        baseDomain = row.stringValue("base_domain"),
        rateLimitPerMinute = row.intValue("rate_limit_per_minute"),
        maxConcurrency = row.intValue("max_concurrency"),
        chapterListSelector = row.stringValue("chapter_list_selector"),
        chapterBodySelector = row.stringValue("chapter_body_selector"),
        adBlocklist = row.stringValue("ad_blocklist").lines().filter { it.isNotBlank() },
        enabled = row.booleanValue("enabled"),
    )

    private fun mapSource(row: Map<String, Any?>): BookSource = BookSource(
        id = row.stringValue("id"),
        siteConfigId = row.stringValue("site_config_id"),
        bookTitle = row.stringValue("book_title"),
        sourceUrl = row.stringValue("source_url"),
        updateIntervalMinutes = row.intValue("update_interval_minutes"),
        lastCheckedAtEpochMinutes = row["last_checked_at_epoch_minutes"]?.toString()?.toLongOrNull(),
    )

    private fun mapTask(row: Map<String, Any?>): PipelineTask = PipelineTask(
        id = row.stringValue("id"),
        type = PipelineTaskType.valueOf(row.stringValue("task_type")),
        status = PipelineTaskStatus.valueOf(row.stringValue("status")),
        targetId = row.stringValue("target_id"),
        retryCount = row.intValue("retry_count"),
        failureReason = row.stringValue("failure_reason").ifBlank { null },
        chaptersFound = Regex("\\d+").find(row.stringValue("payload"))?.value?.toIntOrNull() ?: 0,
    )

    companion object {
        private const val SITE_SQL = """
            select
                id::text as id,
                name,
                base_domain,
                enabled,
                rate_limit_per_minute,
                max_concurrency,
                chapter_list_selector,
                chapter_body_selector,
                ad_blocklist,
                created_at
            from site_config
        """

        private const val SOURCE_SQL = """
            select
                id::text as id,
                site_config_id::text as site_config_id,
                book_title,
                source_url,
                update_interval_minutes,
                last_checked_at_epoch_minutes,
                created_at
            from book_source
        """

        private const val TASK_SQL = """
            select
                id::text as id,
                task_type,
                status,
                target_id,
                retry_count,
                failure_reason,
                payload,
                created_at
            from pipeline_task
        """
    }
}
