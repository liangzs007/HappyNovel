package com.happynovel.reader

sealed class ReaderScreenRoute {
    data object Home : ReaderScreenRoute()
    data object Categories : ReaderScreenRoute()
    data object Bookshelf : ReaderScreenRoute()
    data class BookDetail(val bookId: String) : ReaderScreenRoute()
    data class ChapterCatalog(val bookId: String) : ReaderScreenRoute()
    data class Reader(val bookId: String, val chapterId: String) : ReaderScreenRoute()
}

class ReaderNavigationState(
    initialRoute: ReaderScreenRoute = ReaderScreenRoute.Home,
) {
    private val backStack = mutableListOf(initialRoute)

    val currentRoute: ReaderScreenRoute
        get() = backStack.last()

    fun openHome() {
        resetTo(ReaderScreenRoute.Home)
    }

    fun openCategories() {
        resetTo(ReaderScreenRoute.Categories)
    }

    fun openBookshelf() {
        resetTo(ReaderScreenRoute.Bookshelf)
    }

    fun openBook(bookId: String) {
        push(ReaderScreenRoute.BookDetail(bookId))
    }

    fun openChapters(bookId: String) {
        push(ReaderScreenRoute.ChapterCatalog(bookId))
    }

    fun openReader(bookId: String, chapterId: String) {
        push(ReaderScreenRoute.Reader(bookId, chapterId))
    }

    fun goBack() {
        if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    private fun resetTo(route: ReaderScreenRoute) {
        backStack.clear()
        backStack += route
    }

    private fun push(route: ReaderScreenRoute) {
        backStack += route
    }
}
