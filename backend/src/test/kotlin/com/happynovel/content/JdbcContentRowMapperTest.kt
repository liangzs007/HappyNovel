package com.happynovel.content

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class JdbcContentRowMapperTest {
    @Test
    fun `maps published book row into app summary`() {
        val summary = JdbcContentRowMapper.bookSummary(
            mapOf(
                "id" to "book-seed-1",
                "title" to "Dragon Gate",
                "author" to "Happy Novel Team",
                "cover_url" to "https://example.com/covers/dragon-gate.jpg",
                "description" to "A translated cultivation novel.",
                "serialization_status" to "ongoing",
                "latest_chapter_title" to "Chapter 2: The Trial",
                "updated_at" to "2026-06-08T00:00:00Z",
            ),
        )

        assertEquals("book-seed-1", summary.id)
        assertEquals("Dragon Gate", summary.title)
        assertEquals("ongoing", summary.status)
        assertEquals("Chapter 2: The Trial", summary.latestChapterTitle)
    }

    @Test
    fun `maps translated chapter row with json paragraphs`() {
        val content = JdbcContentRowMapper.chapterContent(
            mapOf(
                "id" to "chapter-seed-1",
                "book_id" to "book-seed-1",
                "title" to "Chapter 1: Azure Cloud Sect",
                "language" to "en",
                "paragraphs" to """["The morning bell echoed across Azure Cloud Sect.","Lin Chen stepped through the Dragon Gate."]""",
            ),
        )

        assertEquals("chapter-seed-1", content.id)
        assertEquals("book-seed-1", content.bookId)
        assertEquals("en", content.language)
        assertEquals(2, content.paragraphs.size)
    }
}
