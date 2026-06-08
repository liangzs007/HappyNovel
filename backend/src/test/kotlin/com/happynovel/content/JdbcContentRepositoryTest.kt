package com.happynovel.content

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JdbcContentRepositoryTest {
    @Test
    fun `repository reads published content through database client`() {
        val repository = JdbcContentRepository(FakeContentDatabaseClient())

        assertEquals("Dragon Gate", repository.homeBooks().single().title)
        assertEquals("Fantasy", repository.categories().single().name)
        assertEquals(listOf("ongoing", "completed"), repository.statuses())
        assertEquals(1, repository.bookDetail("book-seed-1").chapterCount)
        assertEquals("Chapter 1: Azure Cloud Sect", repository.chapterCatalog("book-seed-1").single().title)
        assertEquals("en", repository.chapterContent("chapter-seed-1").language)
    }

    @Test
    fun `home sections use section specific database ordering`() {
        val databaseClient = RecordingContentDatabaseClient()
        val repository = JdbcContentRepository(databaseClient)

        repository.recommendedBooks()
        repository.latestBooks()
        repository.popularBooks()
        repository.newBooks()

        assert(databaseClient.queries[0].contains("recommendation_weight desc"))
        assert(databaseClient.queries[1].contains("b.updated_at desc"))
        assert(databaseClient.queries[2].contains("reading_event"))
        assert(databaseClient.queries[3].contains("b.updated_at desc"))
    }
}

private class RecordingContentDatabaseClient : ContentDatabaseClient {
    val queries = mutableListOf<String>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        queries.add(sql)
        return FakeContentDatabaseClient().query(sql, *args)
    }
}
