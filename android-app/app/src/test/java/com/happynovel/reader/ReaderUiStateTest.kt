package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderUiStateTest {
    @Test
    fun `home screen model exposes english sections and bottom tabs`() {
        val uiState = ReaderUiStateFactory.home(
            HomeState(
                appName = "HappyNovel",
                recommended = listOf(book),
                latestUpdates = listOf(book),
                popular = emptyList(),
                newBooks = emptyList(),
            ),
        )

        assertEquals("HappyNovel", uiState.title)
        assertEquals(listOf("Home", "Categories", "Bookshelf"), uiState.bottomTabs.map { it.label })
        assertEquals(listOf("Recommended", "Latest Updates", "Popular", "New Books"), uiState.sections.map { it.title })
        assertEquals("Dragon Gate", uiState.sections.first().books.single().title)
    }

    @Test
    fun `bookshelf screen model uses empty english copy`() {
        val uiState = ReaderUiStateFactory.bookshelf(BookshelfState.empty())

        assertEquals("Bookshelf", uiState.title)
        assertTrue(uiState.books.isEmpty())
        assertEquals("No saved books yet.", uiState.emptyMessage)
    }

    @Test
    fun `reader screen model formats chapter and settings`() {
        val uiState = ReaderUiStateFactory.reader(
            ReaderScreenState(
                chapter = ChapterContent("chapter-seed-1", "Chapter 1", listOf("First paragraph.")),
                settings = ReaderSettings.default().withFontSize(20),
                progress = ReadingProgress("book-seed-1", "chapter-seed-1", 0.4f),
            ),
        )

        assertEquals("Chapter 1", uiState.title)
        assertEquals(listOf("First paragraph."), uiState.paragraphs)
        assertEquals("20sp", uiState.fontSizeLabel)
        assertEquals("40%", uiState.progressLabel)
    }

    private val book = BookSummary(
        id = "book-seed-1",
        title = "Dragon Gate",
        latestChapterTitle = "Chapter 2: The Trial",
        author = "Happy Novel Team",
    )
}
