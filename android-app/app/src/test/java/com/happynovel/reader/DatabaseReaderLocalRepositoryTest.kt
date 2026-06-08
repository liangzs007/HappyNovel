package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DatabaseReaderLocalRepositoryTest {
    @Test
    fun `repository restores bookshelf settings and chapter cache from database rows`() {
        val database = InMemoryReaderLocalDatabase()
        DatabaseReaderLocalRepository(database).apply {
            saveBook(BookSummary("book-seed-1", "Dragon Gate", "Chapter 2"))
            updateProgress(ReadingProgress("book-seed-1", "chapter-seed-2", 0.64f))
            updateSettings(ReaderSettings.default().withTheme(ReaderTheme.DARK).withFontSize(20))
            cacheChapter(ChapterContent("chapter-seed-2", "Chapter 2", listOf("Cached paragraph.")))
        }

        val restored = DatabaseReaderLocalRepository(database)

        assertTrue(restored.bookshelf().isSaved("book-seed-1"))
        assertEquals("chapter-seed-2", restored.bookshelf().progressFor("book-seed-1")?.chapterId)
        assertEquals(ReaderTheme.DARK, restored.settings().theme)
        assertEquals(20, restored.settings().fontSizeSp)
        assertEquals("Chapter 2", restored.cachedChapter("chapter-seed-2")?.title)
        assertEquals("Cached paragraph.", restored.cachedChapter("chapter-seed-2")?.paragraphs?.single())
    }
}
