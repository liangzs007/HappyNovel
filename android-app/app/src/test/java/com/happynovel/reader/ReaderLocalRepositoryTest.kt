package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderLocalRepositoryTest {
    @Test
    fun `repository starts with empty bookshelf and default settings`() {
        val repository = InMemoryReaderLocalRepository()

        assertTrue(repository.bookshelf().savedBooks.isEmpty())
        assertEquals(ReaderSettings.default(), repository.settings())
    }

    @Test
    fun `repository saves bookshelf entry and reading progress`() {
        val repository = InMemoryReaderLocalRepository()
        val book = BookSummary(
            id = "book-seed-1",
            title = "Dragon Gate",
            latestChapterTitle = "Chapter 2: The Trial",
        )

        repository.saveBook(book)
        repository.updateProgress(ReadingProgress("book-seed-1", "chapter-seed-2", 0.75f))

        assertTrue(repository.bookshelf().isSaved("book-seed-1"))
        assertEquals("chapter-seed-2", repository.bookshelf().progressFor("book-seed-1")?.chapterId)
        assertEquals(0.75f, repository.bookshelf().progressFor("book-seed-1")?.percent)
    }

    @Test
    fun `repository updates reader settings`() {
        val repository = InMemoryReaderLocalRepository()
        val settings = ReaderSettings.default()
            .withFontSize(22)
            .withLineHeight(1.9f)
            .withTheme(ReaderTheme.DARK)
            .withBackground(ReaderBackground.SEPIA)

        repository.updateSettings(settings)

        assertEquals(settings, repository.settings())
    }

    @Test
    fun `repository caches reader chapters for later lookup`() {
        val repository = InMemoryReaderLocalRepository()
        val chapter = ChapterContent(
            id = "chapter-seed-1",
            title = "Chapter 1: Azure Cloud Sect",
            paragraphs = listOf("The morning bell echoed across Azure Cloud Sect."),
        )

        repository.cacheChapter(chapter)

        assertTrue(repository.chapterCache().hasChapter("chapter-seed-1"))
        assertEquals(chapter, repository.cachedChapter("chapter-seed-1"))
        assertFalse(repository.chapterCache().hasChapter("chapter-missing"))
    }
}
