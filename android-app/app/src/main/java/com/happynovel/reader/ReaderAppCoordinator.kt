package com.happynovel.reader

interface ReaderRemoteDataSource {
    fun home(): AppHomeResponseDto

    fun bookDetail(bookId: String): AppBookDetailDto

    fun chapterContent(chapterId: String): AppChapterContentDto
}

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

    fun startReading(bookId: String, chapterId: String): ChapterContent {
        val detail = remoteDataSource.bookDetail(bookId).toBookDetailState()
        val chapter = remoteDataSource.chapterContent(chapterId).toReaderChapter()

        localRepository.saveBook(
            BookSummary(
                id = detail.id,
                title = detail.title,
                author = detail.author,
                coverUrl = detail.coverUrl,
                description = detail.description,
                status = detail.status,
                latestChapterTitle = detail.latestChapter?.title.orEmpty(),
            ),
        )
        localRepository.updateProgress(ReadingProgress(bookId, chapterId, percent = 0f))
        localRepository.cacheChapter(chapter)

        return chapter
    }

    fun readerState(bookId: String, chapterId: String): ReaderScreenState = ReaderScreenState(
        chapter = localRepository.cachedChapter(chapterId),
        settings = localRepository.settings(),
        progress = localRepository.bookshelf().progressFor(bookId),
    )
}
