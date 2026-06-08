package com.happynovel.reader

interface ReaderRemoteDataSource {
    fun home(): AppHomeResponseDto

    fun categories(): AppCategoriesResponseDto

    fun books(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): AppBookListResponseDto = AppBookListResponseDto(home().recommended.take(limit))

    fun bookDetail(bookId: String): AppBookDetailDto

    fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto

    fun chapterContent(chapterId: String): AppChapterContentDto

    fun adConfig(): AppAdConfigDto = AppAdConfigDto(
        enabled = false,
        readerBannerEnabled = false,
        interstitialEveryChapters = 0,
    )

    fun complianceConfig(): AppComplianceConfigDto = AppComplianceConfigDto(
        privacyPolicyTitle = "HappyNovel Privacy Policy",
        termsTitle = "HappyNovel Terms of Service",
        adDisclosureEnabled = false,
        adDisclosureText = "",
    )
}

data class CategoriesState(
    val categories: List<CategorySummary>,
    val statuses: List<String>,
)

data class BookListState(
    val books: List<BookSummary>,
)

data class BookDetailReaderState(
    val book: BookDetailState,
    val isInBookshelf: Boolean,
    val progress: ReadingProgress?,
)

data class ChapterCatalogState(
    val bookId: String,
    val chapters: List<ChapterSummary>,
    val currentChapterId: String?,
)

data class ReaderScreenState(
    val chapter: ChapterContent?,
    val settings: ReaderSettings,
    val progress: ReadingProgress?,
)

class ReaderAppCoordinator(
    private val remoteDataSource: ReaderRemoteDataSource,
    private val localRepository: ReaderLocalRepository,
) {
    fun loadHome(): HomeState = remoteDataSource.home().toHomeState()

    fun loadCategories(): CategoriesState {
        val response = remoteDataSource.categories()
        return CategoriesState(
            categories = response.categories.map { it.toCategorySummary() },
            statuses = response.statuses,
        )
    }

    fun loadBookshelf(): BookshelfState = localRepository.bookshelf()

    fun loadBooks(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): BookListState = BookListState(
        books = remoteDataSource.books(category, status, sort, limit).books.map { it.toReaderBookSummary() },
    )

    fun loadBookDetail(bookId: String): BookDetailReaderState {
        val detail = remoteDataSource.bookDetail(bookId).toBookDetailState()
        return BookDetailReaderState(
            book = detail,
            isInBookshelf = localRepository.bookshelf().isSaved(bookId),
            progress = localRepository.bookshelf().progressFor(bookId),
        )
    }

    fun loadChapterCatalog(bookId: String): ChapterCatalogState {
        val response = remoteDataSource.chapterCatalog(bookId)
        return ChapterCatalogState(
            bookId = response.bookId,
            chapters = response.chapters.map { it.toChapterSummary() },
            currentChapterId = localRepository.bookshelf().progressFor(bookId)?.chapterId,
        )
    }

    fun startReading(bookId: String, chapterId: String): ChapterContent {
        val detail = remoteDataSource.bookDetail(bookId).toBookDetailState()
        val chapter = remoteDataSource.chapterContent(chapterId).toReaderChapter()

        localRepository.saveBook(detail.toBookSummary())
        localRepository.updateProgress(ReadingProgress(bookId, chapterId, percent = 0f))
        localRepository.cacheChapter(chapter)

        return chapter
    }

    fun saveBookToBookshelf(bookId: String) {
        val detail = remoteDataSource.bookDetail(bookId).toBookDetailState()
        localRepository.saveBook(detail.toBookSummary())
    }

    fun updateReadingProgress(bookId: String, chapterId: String, percent: Float) {
        localRepository.updateProgress(
            ReadingProgress(
                bookId = bookId,
                chapterId = chapterId,
                percent = percent.coerceIn(0f, 1f),
            ),
        )
    }

    fun increaseFontSize() {
        updateSettings { current ->
            current.withFontSize((current.fontSizeSp + FONT_SIZE_STEP_SP).coerceAtMost(MAX_FONT_SIZE_SP))
        }
    }

    fun decreaseFontSize() {
        updateSettings { current ->
            current.withFontSize((current.fontSizeSp - FONT_SIZE_STEP_SP).coerceAtLeast(MIN_FONT_SIZE_SP))
        }
    }

    fun toggleTheme() {
        updateSettings { current ->
            current.withTheme(
                if (current.theme == ReaderTheme.LIGHT) ReaderTheme.DARK else ReaderTheme.LIGHT,
            )
        }
    }

    fun readerState(bookId: String, chapterId: String): ReaderScreenState = ReaderScreenState(
        chapter = localRepository.cachedChapter(chapterId),
        settings = localRepository.settings(),
        progress = localRepository.bookshelf().progressFor(bookId),
    )

    private fun updateSettings(transform: (ReaderSettings) -> ReaderSettings) {
        localRepository.updateSettings(transform(localRepository.settings()))
    }

    private companion object {
        const val MIN_FONT_SIZE_SP = 14
        const val MAX_FONT_SIZE_SP = 28
        const val FONT_SIZE_STEP_SP = 2
    }
}

private fun BookDetailState.toBookSummary(): BookSummary = BookSummary(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    latestChapterTitle = latestChapter?.title.orEmpty(),
)
