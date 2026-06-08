package com.happynovel.reader

data class ReaderLaunchState(
    val home: HomeUiState,
    val categories: CategoriesUiState,
    val bookshelf: BookshelfUiState,
    val bookDetail: BookDetailUiState,
    val chapterCatalog: ChapterCatalogUiState,
    val reader: ReaderUiState,
)

object ReaderLaunchStateFactory {
    fun create(
        remoteDataSource: ReaderRemoteDataSource = SeedReaderRemoteDataSource(),
        localRepository: ReaderLocalRepository = InMemoryReaderLocalRepository(),
        bookId: String = "book-seed-1",
        chapterId: String = "chapter-seed-1",
    ): ReaderLaunchState {
        val coordinator = ReaderAppCoordinator(remoteDataSource, localRepository)
        coordinator.startReading(bookId, chapterId)

        return ReaderLaunchState(
            home = ReaderUiStateFactory.home(coordinator.loadHome()),
            categories = ReaderUiStateFactory.categories(coordinator.loadCategories()),
            bookshelf = ReaderUiStateFactory.bookshelf(localRepository.bookshelf()),
            bookDetail = ReaderUiStateFactory.bookDetail(coordinator.loadBookDetail(bookId)),
            chapterCatalog = ReaderUiStateFactory.chapterCatalog(coordinator.loadChapterCatalog(bookId)),
            reader = ReaderUiStateFactory.reader(coordinator.readerState(bookId, chapterId)),
        )
    }
}
