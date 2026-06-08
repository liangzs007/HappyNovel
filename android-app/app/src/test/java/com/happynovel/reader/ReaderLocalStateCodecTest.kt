package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderLocalStateCodecTest {
    @Test
    fun `codec round trips bookshelf settings and cache`() {
        val state = ReaderLocalState(
            bookshelf = BookshelfState.empty()
                .saveBook(
                    BookSummary(
                        id = "book-seed-1",
                        title = "Dragon Gate",
                        latestChapterTitle = "Chapter 2: The Trial",
                        author = "Happy Novel Team",
                    ),
                )
                .updateProgress(ReadingProgress("book-seed-1", "chapter-seed-2", 0.75f)),
            settings = ReaderSettings.default()
                .withFontSize(22)
                .withTheme(ReaderTheme.DARK)
                .withBackground(ReaderBackground.SEPIA),
            cache = ChapterCache.empty().preload(
                current = ChapterContent(
                    id = "chapter-seed-2",
                    title = "Chapter 2: The Trial",
                    paragraphs = listOf("The first trial began before sunrise."),
                ),
            ),
        )

        val decoded = ReaderLocalStateCodec.decode(ReaderLocalStateCodec.encode(state))

        assertTrue(decoded.bookshelf.isSaved("book-seed-1"))
        assertEquals("chapter-seed-2", decoded.bookshelf.progressFor("book-seed-1")?.chapterId)
        assertEquals(22, decoded.settings.fontSizeSp)
        assertEquals(ReaderTheme.DARK, decoded.settings.theme)
        assertEquals(ReaderBackground.SEPIA, decoded.settings.background)
        assertEquals("Chapter 2: The Trial", decoded.cache.chapters["chapter-seed-2"]?.title)
    }

    @Test
    fun `codec decodes blank input as default state`() {
        val state = ReaderLocalStateCodec.decode("")

        assertTrue(state.bookshelf.savedBooks.isEmpty())
        assertEquals(ReaderSettings.default(), state.settings)
        assertTrue(state.cache.chapters.isEmpty())
    }
}
