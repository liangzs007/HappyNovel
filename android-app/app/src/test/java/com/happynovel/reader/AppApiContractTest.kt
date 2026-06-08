package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppApiContractTest {
    @Test
    fun `api routes match backend app endpoints`() {
        val routes = HappyNovelApiRoutes(baseUrl = "https://api.happynovel.example")

        assertEquals("https://api.happynovel.example/api/app/home", routes.home())
        assertEquals("https://api.happynovel.example/api/app/categories", routes.categories())
        assertEquals("https://api.happynovel.example/api/app/books/book-seed-1", routes.bookDetail("book-seed-1"))
        assertEquals(
            "https://api.happynovel.example/api/app/books/book-seed-1/chapters",
            routes.chapterCatalog("book-seed-1"),
        )
        assertEquals("https://api.happynovel.example/api/app/chapters/chapter-seed-1", routes.chapterContent("chapter-seed-1"))
        assertEquals("https://api.happynovel.example/api/app/ad-config", routes.adConfig())
    }

    @Test
    fun `home response maps backend sections into reader home state`() {
        val book = AppBookSummaryDto(
            id = "book-seed-1",
            title = "Dragon Gate",
            author = "Happy Novel Team",
            coverUrl = "https://example.com/covers/dragon-gate.jpg",
            description = "A translated cultivation novel prepared for MVP API validation.",
            status = "ongoing",
            latestChapterTitle = "Chapter 2: The Trial",
            updatedAt = "2026-06-08T00:00:00Z",
        )
        val state = AppHomeResponseDto(
            appName = "HappyNovel",
            recommended = listOf(book),
            latestUpdates = listOf(book),
            popular = listOf(book),
            newBooks = listOf(book),
        ).toHomeState()

        assertEquals("HappyNovel", state.appName)
        assertEquals("Dragon Gate", state.latestUpdates.single().title)
        assertEquals("Happy Novel Team", state.latestUpdates.single().author)
        assertEquals("Chapter 2: The Trial", state.latestUpdates.single().latestChapterTitle)
    }

    @Test
    fun `chapter response maps translated paragraphs for reader cache`() {
        val chapter = AppChapterContentDto(
            id = "chapter-seed-1",
            bookId = "book-seed-1",
            title = "Chapter 1: Azure Cloud Sect",
            language = "en",
            paragraphs = listOf("The morning bell echoed across Azure Cloud Sect."),
        ).toReaderChapter()

        assertEquals("chapter-seed-1", chapter.id)
        assertEquals("Chapter 1: Azure Cloud Sect", chapter.title)
        assertTrue(chapter.paragraphs.first().contains("Azure Cloud"))
    }
}
