package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderStateTest {
    @Test
    fun `default navigation exposes Home Categories and Bookshelf`() {
        assertEquals(
            listOf("Home", "Categories", "Bookshelf"),
            ReaderNavigation.primaryTabs.map { it.label }
        )
    }

    @Test
    fun `reader settings update font line height theme and background`() {
        val settings = ReaderSettings.default()
            .withFontSize(20)
            .withLineHeight(1.8f)
            .withTheme(ReaderTheme.DARK)
            .withBackground(ReaderBackground.SEPIA)

        assertEquals(20, settings.fontSizeSp)
        assertEquals(1.8f, settings.lineHeightMultiplier)
        assertEquals(ReaderTheme.DARK, settings.theme)
        assertEquals(ReaderBackground.SEPIA, settings.background)
    }

    @Test
    fun `bookshelf stores saved book and reading progress`() {
        val bookshelf = BookshelfState.empty()
            .saveBook(BookSummary("book-1", "Dragon Gate", "Chapter 2"))
            .updateProgress(ReadingProgress("book-1", "chapter-2", 0.42f))

        assertTrue(bookshelf.isSaved("book-1"))
        assertEquals("chapter-2", bookshelf.progressFor("book-1")?.chapterId)
        assertEquals(0.42f, bookshelf.progressFor("book-1")?.percent)
    }

    @Test
    fun `cache preloads current adjacent and recent chapters`() {
        val cache = ChapterCache.empty()
            .preload(
                current = ChapterContent("chapter-2", "Chapter 2", listOf("Current")),
                previous = ChapterContent("chapter-1", "Chapter 1", listOf("Previous")),
                next = ChapterContent("chapter-3", "Chapter 3", listOf("Next")),
            )

        assertTrue(cache.hasChapter("chapter-1"))
        assertTrue(cache.hasChapter("chapter-2"))
        assertTrue(cache.hasChapter("chapter-3"))
        assertFalse(cache.hasChapter("chapter-4"))
    }
}
