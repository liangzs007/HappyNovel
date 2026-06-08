package com.happynovel.reader

interface ReaderLocalRepository {
    fun bookshelf(): BookshelfState

    fun saveBook(book: BookSummary)

    fun updateProgress(progress: ReadingProgress)

    fun settings(): ReaderSettings

    fun updateSettings(settings: ReaderSettings)

    fun chapterCache(): ChapterCache

    fun cacheChapter(chapter: ChapterContent)

    fun cachedChapter(chapterId: String): ChapterContent?
}

class InMemoryReaderLocalRepository(
    private var bookshelfState: BookshelfState = BookshelfState.empty(),
    private var readerSettings: ReaderSettings = ReaderSettings.default(),
    private var cache: ChapterCache = ChapterCache.empty(),
) : ReaderLocalRepository {
    override fun bookshelf(): BookshelfState = bookshelfState

    override fun saveBook(book: BookSummary) {
        bookshelfState = bookshelfState.saveBook(book)
    }

    override fun updateProgress(progress: ReadingProgress) {
        bookshelfState = bookshelfState.updateProgress(progress)
    }

    override fun settings(): ReaderSettings = readerSettings

    override fun updateSettings(settings: ReaderSettings) {
        readerSettings = settings
    }

    override fun chapterCache(): ChapterCache = cache

    override fun cacheChapter(chapter: ChapterContent) {
        cache = cache.preload(current = chapter)
    }

    override fun cachedChapter(chapterId: String): ChapterContent? = cache.chapters[chapterId]
}
