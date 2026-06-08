package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenLoadStateTest {
    @Test
    fun `loading state exposes english loading message`() {
        val state = ScreenLoadState.loading<HomeUiState>()

        assertTrue(state.isLoading)
        assertEquals("Loading...", state.message)
    }

    @Test
    fun `error state exposes screen specific retry copy`() {
        val state = ScreenLoadState.error<HomeUiState>("Unable to load books. Try again.")

        assertFalse(state.isLoading)
        assertTrue(state.shouldShowRetry)
        assertEquals("Unable to load books. Try again.", state.message)
    }

    @Test
    fun `empty state keeps english empty copy`() {
        val state = ScreenLoadState.empty<BookshelfUiState>("No saved books yet.")

        assertFalse(state.isLoading)
        assertEquals("No saved books yet.", state.message)
    }

    @Test
    fun `content state wraps screen ui`() {
        val content = ReaderUiStateFactory.bookshelf(BookshelfState.empty())
        val state = ScreenLoadState.content(content)

        assertEquals(content, state.content)
        assertEquals("", state.message)
    }
}
