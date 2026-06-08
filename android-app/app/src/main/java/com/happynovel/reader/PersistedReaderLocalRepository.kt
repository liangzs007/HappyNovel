package com.happynovel.reader

interface ReaderStateStore {
    fun read(): String

    fun write(value: String)
}

class InMemoryReaderStateStore(
    private var value: String = "",
) : ReaderStateStore {
    override fun read(): String = value

    override fun write(value: String) {
        this.value = value
    }
}

class PersistedReaderLocalRepository(
    private val store: ReaderStateStore,
) : ReaderLocalRepository {
    private var state: ReaderLocalState = ReaderLocalStateCodec.decode(store.read())

    override fun bookshelf(): BookshelfState = state.bookshelf

    override fun saveBook(book: BookSummary) {
        update(state.copy(bookshelf = state.bookshelf.saveBook(book)))
    }

    override fun updateProgress(progress: ReadingProgress) {
        update(state.copy(bookshelf = state.bookshelf.updateProgress(progress)))
    }

    override fun settings(): ReaderSettings = state.settings

    override fun updateSettings(settings: ReaderSettings) {
        update(state.copy(settings = settings))
    }

    override fun chapterCache(): ChapterCache = state.cache

    override fun cacheChapter(chapter: ChapterContent) {
        update(state.copy(cache = state.cache.preload(current = chapter)))
    }

    override fun cachedChapter(chapterId: String): ChapterContent? = state.cache.chapters[chapterId]

    private fun update(newState: ReaderLocalState) {
        state = newState
        store.write(ReaderLocalStateCodec.encode(newState))
    }
}
