package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistedReaderLocalRepositoryTest {
    @Test
    fun `repository restores saved state from store`() {
        val store = InMemoryReaderStateStore()
        PersistedReaderLocalRepository(store).apply {
            saveBook(BookSummary("book-seed-1", "Dragon Gate", "Chapter 2"))
            updateProgress(ReadingProgress("book-seed-1", "chapter-seed-2", 0.6f))
            updateSettings(ReaderSettings.default().withFontSize(21))
            cacheChapter(ChapterContent("chapter-seed-2", "Chapter 2", listOf("Cached paragraph.")))
        }

        val restored = PersistedReaderLocalRepository(store)

        assertTrue(restored.bookshelf().isSaved("book-seed-1"))
        assertEquals("chapter-seed-2", restored.bookshelf().progressFor("book-seed-1")?.chapterId)
        assertEquals(21, restored.settings().fontSizeSp)
        assertEquals("Chapter 2", restored.cachedChapter("chapter-seed-2")?.title)
    }
}
