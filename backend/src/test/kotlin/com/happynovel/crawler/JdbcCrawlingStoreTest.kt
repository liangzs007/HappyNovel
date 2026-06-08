package com.happynovel.crawler

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbcCrawlingStoreTest {
    @Test
    fun `jdbc crawling store persists site source and tasks`() {
        val database = RecordingCrawlingDatabaseClient()
        val store = JdbcCrawlingStore(database)
        val site = SiteConfig(
            id = SITE_ID,
            name = "示例站点",
            baseDomain = "https://novels.example.com",
            rateLimitPerMinute = 30,
            maxConcurrency = 2,
            chapterListSelector = ".chapter-list a",
            chapterBodySelector = ".chapter-content",
            adBlocklist = listOf("请收藏本站", "最新网址"),
        )
        val source = BookSource(
            id = SOURCE_ID,
            siteConfigId = SITE_ID,
            bookTitle = "测试小说",
            sourceUrl = "https://novels.example.com/book/1",
            updateIntervalMinutes = 360,
        )
        val task = PipelineTask(
            id = TASK_ID,
            type = PipelineTaskType.CRAWL_LATEST,
            status = PipelineTaskStatus.CREATED,
            targetId = SOURCE_ID,
        )

        store.saveSiteConfig(site)
        store.saveBookSource(source)
        store.saveTask(task)

        assertEquals(listOf(site), store.siteConfigs())
        assertEquals(listOf(source), store.bookSources())
        assertEquals(listOf(task), store.tasks())
        assertTrue(database.updates.any { it.contains("insert into site_config") })
        assertTrue(database.updates.any { it.contains("insert into book_source") })
        assertTrue(database.updates.any { it.contains("insert into pipeline_task") })
    }
}

private const val SITE_ID = "00000000-0000-0000-0000-000000000010"
private const val SOURCE_ID = "00000000-0000-0000-0000-000000000020"
private const val TASK_ID = "00000000-0000-0000-0000-000000000030"

private class RecordingCrawlingDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val sites = mutableListOf<Map<String, Any?>>()
    private val sources = mutableListOf<Map<String, Any?>>()
    private val tasks = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> =
        when {
            sql.contains("from site_config") -> sites
            sql.contains("from book_source") -> sources
            sql.contains("from pipeline_task") -> tasks
            else -> emptyList()
        }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        when {
            sql.contains("insert into site_config") -> {
                sites += mapOf(
                    "id" to args[0].toString(),
                    "name" to args[1],
                    "base_domain" to args[2],
                    "enabled" to args[3],
                    "rate_limit_per_minute" to args[4],
                    "max_concurrency" to args[5],
                    "chapter_list_selector" to args[6],
                    "chapter_body_selector" to args[7],
                    "ad_blocklist" to args[8],
                )
            }
            sql.contains("insert into book_source") -> {
                sources += mapOf(
                    "id" to args[0].toString(),
                    "site_config_id" to args[1].toString(),
                    "book_title" to args[2],
                    "source_url" to args[3],
                    "update_interval_minutes" to args[4],
                    "last_checked_at_epoch_minutes" to args[5],
                )
            }
            sql.contains("insert into pipeline_task") -> {
                tasks += mapOf(
                    "id" to args[0].toString(),
                    "task_type" to args[1],
                    "status" to args[2],
                    "target_id" to args[4],
                    "retry_count" to args[5],
                    "failure_reason" to args[6],
                    "payload" to args[7],
                )
            }
        }
        return 1
    }
}
