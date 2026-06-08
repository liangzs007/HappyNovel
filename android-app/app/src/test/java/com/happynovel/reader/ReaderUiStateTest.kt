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
        val uiState = ReaderUiStateFactory.bookshelf(
            BookshelfState.empty()
                .saveBook(book)
                .updateProgress(ReadingProgress(book.id, "chapter-seed-1", 0.4f)),
        )

        assertEquals("Bookshelf", uiState.title)
        assertEquals("Dragon Gate", uiState.books.single().title)
        assertEquals("40%", uiState.progressFor(book.id)?.progressLabel)
        assertEquals("chapter-seed-1", uiState.progressFor(book.id)?.chapterId)
        assertEquals("No saved books yet.", uiState.emptyMessage)
    }

    @Test
    fun `categories screen model exposes genre and status filters`() {
        val uiState = ReaderUiStateFactory.categories(
            CategoriesState(
                categories = listOf(CategorySummary("category-fantasy", "Fantasy", "fantasy")),
                statuses = listOf("ongoing", "completed"),
            ),
        )

        assertEquals("Categories", uiState.title)
        assertEquals("Genre", uiState.filterLabels.first())
        assertEquals("Fantasy", uiState.categories.single().name)
        assertEquals(listOf("ongoing", "completed"), uiState.statuses)
    }

    @Test
    fun `book detail screen model changes action for saved book`() {
        val uiState = ReaderUiStateFactory.bookDetail(
            BookDetailReaderState(
                book = BookDetailState(
                    id = "book-seed-1",
                    title = "Dragon Gate",
                    author = "Happy Novel Team",
                    coverUrl = "",
                    description = "A translated cultivation novel.",
                    status = "ongoing",
                    categories = listOf(CategorySummary("category-fantasy", "Fantasy", "fantasy")),
                    chapterCount = 1,
                    latestChapter = ChapterSummary("chapter-seed-1", 1, "Chapter 1", "2026-06-08T00:00:00Z"),
                ),
                isInBookshelf = true,
                progress = ReadingProgress("book-seed-1", "chapter-seed-1", 0.5f),
            ),
        )

        assertEquals("Dragon Gate", uiState.title)
        assertEquals("Continue Reading", uiState.primaryAction)
        assertEquals("In Bookshelf", uiState.bookshelfAction)
        assertEquals("1 chapters", uiState.chapterCountLabel)
        assertEquals("chapter-seed-1", uiState.latestChapterId)
    }

    @Test
    fun `chapter catalog screen model marks current chapter`() {
        val uiState = ReaderUiStateFactory.chapterCatalog(
            ChapterCatalogState(
                bookId = "book-seed-1",
                chapters = listOf(ChapterSummary("chapter-seed-1", 1, "Chapter 1", "2026-06-08T00:00:00Z")),
                currentChapterId = "chapter-seed-1",
            ),
        )

        assertEquals("Chapters", uiState.title)
        assertEquals("Chapter 1", uiState.chapters.single().title)
        assertTrue(uiState.chapters.single().isCurrent)
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
        assertEquals(20, uiState.fontSizeSp)
        assertEquals(ReaderTheme.LIGHT, uiState.theme)
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
