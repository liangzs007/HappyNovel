package com.happynovel.reader

import kotlin.math.roundToInt

data class HomeUiState(
    val title: String,
    val bottomTabs: List<NavigationTab>,
    val sections: List<BookSectionUiState>,
)

data class BookSectionUiState(
    val title: String,
    val books: List<BookSummary>,
)

data class BookshelfUiState(
    val title: String,
    val books: List<BookSummary>,
    val emptyMessage: String,
)

data class ReaderUiState(
    val title: String,
    val paragraphs: List<String>,
    val fontSizeLabel: String,
    val progressLabel: String,
)

object ReaderUiStateFactory {
    fun home(homeState: HomeState): HomeUiState = HomeUiState(
        title = homeState.appName,
        bottomTabs = ReaderNavigation.primaryTabs,
        sections = listOf(
            BookSectionUiState("Recommended", homeState.recommended),
            BookSectionUiState("Latest Updates", homeState.latestUpdates),
            BookSectionUiState("Popular", homeState.popular),
            BookSectionUiState("New Books", homeState.newBooks),
        ),
    )

    fun bookshelf(bookshelfState: BookshelfState): BookshelfUiState = BookshelfUiState(
        title = "Bookshelf",
        books = bookshelfState.savedBooks,
        emptyMessage = "No saved books yet.",
    )

    fun reader(readerState: ReaderScreenState): ReaderUiState = ReaderUiState(
        title = readerState.chapter?.title ?: "This chapter is unavailable.",
        paragraphs = readerState.chapter?.paragraphs ?: emptyList(),
        fontSizeLabel = "${readerState.settings.fontSizeSp}sp",
        progressLabel = "${((readerState.progress?.percent ?: 0f) * 100).roundToInt()}%",
    )
}
