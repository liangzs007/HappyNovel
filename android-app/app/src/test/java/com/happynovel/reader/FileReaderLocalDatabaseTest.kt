package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FileReaderLocalDatabaseTest {
    @Test
    fun `database restores local reader rows from file`() {
        val file = File.createTempFile("reader-local", ".json").apply { deleteOnExit() }
        FileReaderLocalDatabase(file).apply {
            upsertBook(ReaderBookRow("book-seed-1", "Dragon Gate", "Chapter 2", "", "", "", "ongoing", "2026-06-08"))
            upsertProgress(ReaderProgressRow("book-seed-1", "chapter-seed-2", 0.7f))
            upsertSettings(ReaderSettingsRow(21, 1.8f, ReaderTheme.DARK.name, ReaderBackground.SEPIA.name))
            upsertChapter(ReaderChapterRow("chapter-seed-2", "Chapter 2", listOf("Cached paragraph.")))
        }

        val restored = FileReaderLocalDatabase(file)

        assertEquals("Dragon Gate", restored.books().single().title)
        assertEquals("chapter-seed-2", restored.progressRows().single().chapterId)
        assertEquals(ReaderTheme.DARK.name, restored.settings()?.theme)
        assertTrue(restored.chapters().single().paragraphs.contains("Cached paragraph."))
    }
}
