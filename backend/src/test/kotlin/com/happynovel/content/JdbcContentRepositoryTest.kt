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
}

private class FakeContentDatabaseClient : ContentDatabaseClient {
    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        return when {
            sql.contains("from taxonomy_category") -> listOf(categoryRow)
            sql.contains("from book b") -> listOf(bookRow)
            sql.contains("ct.paragraphs") -> listOf(chapterContentRow)
            sql.contains("from chapter") -> listOf(chapterRow)
            else -> emptyList()
        }
    }

    private val categoryRow = mapOf(
        "id" to "category-fantasy",
        "name" to "Fantasy",
        "slug" to "fantasy",
    )

    private val bookRow = mapOf(
        "id" to "book-seed-1",
        "title" to "Dragon Gate",
        "author" to "Happy Novel Team",
        "cover_url" to "https://example.com/covers/dragon-gate.jpg",
        "description" to "A translated cultivation novel.",
        "serialization_status" to "ongoing",
        "latest_chapter_title" to "Chapter 1: Azure Cloud Sect",
        "updated_at" to "2026-06-08T00:00:00Z",
    )

    private val chapterRow = mapOf(
        "id" to "chapter-seed-1",
        "chapter_order" to 1,
        "title" to "Chapter 1: Azure Cloud Sect",
        "updated_at" to "2026-06-08T00:00:00Z",
    )

    private val chapterContentRow = mapOf(
        "id" to "chapter-seed-1",
        "book_id" to "book-seed-1",
        "title" to "Chapter 1: Azure Cloud Sect",
        "language" to "en",
        "paragraphs" to """["The morning bell echoed across Azure Cloud Sect."]""",
    )
}
