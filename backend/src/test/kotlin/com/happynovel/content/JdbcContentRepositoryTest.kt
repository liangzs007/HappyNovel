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
