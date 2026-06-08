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

data class CategoriesUiState(
    val title: String,
    val filterLabels: List<String>,
    val categories: List<CategorySummary>,
    val statuses: List<String>,
)

data class BookListUiState(
    val title: String,
    val books: List<BookSummary>,
    val emptyMessage: String,
)

data class BookDetailUiState(
    val title: String,
    val author: String,
    val description: String,
    val status: String,
    val categories: List<CategorySummary>,
    val primaryAction: String,
    val bookshelfAction: String,
    val chapterCountLabel: String,
    val latestChapterId: String?,
)

data class ChapterCatalogUiState(
    val title: String,
    val chapters: List<ChapterRowUiState>,
)

data class ChapterRowUiState(
    val id: String,
    val order: Int,
    val title: String,
    val isCurrent: Boolean,
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

    fun categories(categoriesState: CategoriesState): CategoriesUiState = CategoriesUiState(
        title = "Categories",
        filterLabels = listOf("Genre", "Status", "Sort"),
        categories = categoriesState.categories,
        statuses = categoriesState.statuses,
    )

    fun bookList(bookListState: BookListState): BookListUiState = BookListUiState(
        title = "Book List",
        books = bookListState.books,
        emptyMessage = "No books found.",
    )

    fun bookDetail(bookDetailState: BookDetailReaderState): BookDetailUiState = BookDetailUiState(
        title = bookDetailState.book.title,
        author = bookDetailState.book.author,
        description = bookDetailState.book.description,
        status = bookDetailState.book.status,
        categories = bookDetailState.book.categories,
        primaryAction = if (bookDetailState.progress == null) "Start Reading" else "Continue Reading",
        bookshelfAction = if (bookDetailState.isInBookshelf) "In Bookshelf" else "Add to Bookshelf",
        chapterCountLabel = "${bookDetailState.book.chapterCount} chapters",
        latestChapterId = bookDetailState.book.latestChapter?.id,
    )

    fun chapterCatalog(chapterCatalogState: ChapterCatalogState): ChapterCatalogUiState = ChapterCatalogUiState(
        title = "Chapters",
        chapters = chapterCatalogState.chapters.map {
            ChapterRowUiState(
                id = it.id,
                order = it.order,
                title = it.title,
                isCurrent = it.id == chapterCatalogState.currentChapterId,
            )
        },
    )

    fun reader(readerState: ReaderScreenState): ReaderUiState = ReaderUiState(
        title = readerState.chapter?.title ?: "This chapter is unavailable.",
        paragraphs = readerState.chapter?.paragraphs ?: emptyList(),
        fontSizeLabel = "${readerState.settings.fontSizeSp}sp",
        progressLabel = "${((readerState.progress?.percent ?: 0f) * 100).roundToInt()}%",
    )
}
