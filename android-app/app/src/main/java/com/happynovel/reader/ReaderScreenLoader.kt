package com.happynovel.reader

class ReaderScreenLoader(
    private val coordinator: ReaderAppCoordinator,
) {
    fun home(): ScreenLoadState<HomeUiState> = runCatching {
        ReaderUiStateFactory.home(coordinator.loadHome())
    }.fold(
        onSuccess = { home ->
            val hasBooks = home.sections.any { section -> section.books.isNotEmpty() }
            if (hasBooks) {
                ScreenLoadState.content(home)
            } else {
                ScreenLoadState.empty("No books found.")
            }
        },
        onFailure = { ScreenLoadState.error("Unable to load books. Try again.") },
    )

    fun categories(): ScreenLoadState<CategoriesUiState> = runCatching {
        ReaderUiStateFactory.categories(coordinator.loadCategories())
    }.fold(
        onSuccess = { categories ->
            if (categories.categories.isEmpty()) {
                ScreenLoadState.empty("No categories found.")
            } else {
                ScreenLoadState.content(categories)
            }
        },
        onFailure = { ScreenLoadState.error("Unable to load categories. Try again.") },
    )

    fun bookDetail(bookId: String): ScreenLoadState<BookDetailUiState> = runCatching {
        ReaderUiStateFactory.bookDetail(coordinator.loadBookDetail(bookId))
    }.fold(
        onSuccess = { ScreenLoadState.content(it) },
        onFailure = { ScreenLoadState.error("Unable to load book details. Try again.") },
    )

    fun chapterCatalog(bookId: String): ScreenLoadState<ChapterCatalogUiState> = runCatching {
        ReaderUiStateFactory.chapterCatalog(coordinator.loadChapterCatalog(bookId))
    }.fold(
        onSuccess = { catalog ->
            if (catalog.chapters.isEmpty()) {
                ScreenLoadState.empty("No chapters found.")
            } else {
                ScreenLoadState.content(catalog)
            }
        },
        onFailure = { ScreenLoadState.error("Unable to load chapters. Try again.") },
    )

    fun reader(bookId: String, chapterId: String): ScreenLoadState<ReaderUiState> = runCatching {
        ReaderUiStateFactory.reader(coordinator.readerState(bookId, chapterId))
    }.fold(
        onSuccess = { reader ->
            if (reader.paragraphs.isEmpty()) {
                ScreenLoadState.empty("This chapter is unavailable.")
            } else {
                ScreenLoadState.content(reader)
            }
        },
        onFailure = { ScreenLoadState.error("This chapter is unavailable.") },
    )
}
