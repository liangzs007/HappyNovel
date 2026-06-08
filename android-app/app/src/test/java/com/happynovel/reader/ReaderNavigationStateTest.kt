package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderNavigationStateTest {
    @Test
    fun `navigation starts on home and opens detail catalog and reader`() {
        val state = ReaderNavigationState()

        assertEquals(ReaderScreenRoute.Home, state.currentRoute)

        state.openBook("book-seed-1")
        assertEquals(ReaderScreenRoute.BookDetail("book-seed-1"), state.currentRoute)

        state.openChapters("book-seed-1")
        assertEquals(ReaderScreenRoute.ChapterCatalog("book-seed-1"), state.currentRoute)

        state.openReader("book-seed-1", "chapter-seed-1")
        assertEquals(ReaderScreenRoute.Reader("book-seed-1", "chapter-seed-1"), state.currentRoute)
    }

    @Test
    fun `back returns to previous route and stops at root`() {
        val state = ReaderNavigationState()

        state.openBook("book-seed-1")
        state.openChapters("book-seed-1")

        state.goBack()
        assertEquals(ReaderScreenRoute.BookDetail("book-seed-1"), state.currentRoute)

        state.goBack()
        assertEquals(ReaderScreenRoute.Home, state.currentRoute)

        state.goBack()
        assertEquals(ReaderScreenRoute.Home, state.currentRoute)
    }
}
