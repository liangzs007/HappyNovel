package com.happynovel.reader

data class NavigationTab(
    val key: String,
    val label: String,
)

object ReaderNavigation {
    val primaryTabs: List<NavigationTab> = listOf(
        NavigationTab("home", "Home"),
        NavigationTab("categories", "Categories"),
        NavigationTab("bookshelf", "Bookshelf"),
    )
}

enum class ReaderTheme {
    LIGHT,
    DARK,
}

enum class ReaderBackground {
    DEFAULT,
    SEPIA,
}

data class ReaderSettings(
    val fontSizeSp: Int,
    val lineHeightMultiplier: Float,
    val theme: ReaderTheme,
    val background: ReaderBackground,
) {
    fun withFontSize(fontSizeSp: Int): ReaderSettings = copy(fontSizeSp = fontSizeSp)

    fun withLineHeight(lineHeightMultiplier: Float): ReaderSettings =
        copy(lineHeightMultiplier = lineHeightMultiplier)

    fun withTheme(theme: ReaderTheme): ReaderSettings = copy(theme = theme)

    fun withBackground(background: ReaderBackground): ReaderSettings = copy(background = background)

    companion object {
        fun default(): ReaderSettings = ReaderSettings(
            fontSizeSp = 18,
            lineHeightMultiplier = 1.6f,
            theme = ReaderTheme.LIGHT,
            background = ReaderBackground.DEFAULT,
        )
    }
}

data class BookSummary(
    val id: String,
    val title: String,
    val latestChapterTitle: String,
)

data class ReadingProgress(
    val bookId: String,
    val chapterId: String,
    val percent: Float,
)

data class BookshelfState(
    val savedBooks: List<BookSummary>,
    val progress: Map<String, ReadingProgress>,
) {
    fun saveBook(book: BookSummary): BookshelfState {
        val updatedBooks = savedBooks.filterNot { it.id == book.id } + book
        return copy(savedBooks = updatedBooks)
    }

    fun updateProgress(readingProgress: ReadingProgress): BookshelfState =
        copy(progress = progress + (readingProgress.bookId to readingProgress))

    fun isSaved(bookId: String): Boolean = savedBooks.any { it.id == bookId }

    fun progressFor(bookId: String): ReadingProgress? = progress[bookId]

    companion object {
        fun empty(): BookshelfState = BookshelfState(
            savedBooks = emptyList(),
            progress = emptyMap(),
        )
    }
}

data class ChapterContent(
    val id: String,
    val title: String,
    val paragraphs: List<String>,
)

data class ChapterCache(
    val chapters: Map<String, ChapterContent>,
) {
    fun preload(
        current: ChapterContent,
        previous: ChapterContent? = null,
        next: ChapterContent? = null,
    ): ChapterCache {
        val loaded = listOfNotNull(previous, current, next).associateBy { it.id }
        return copy(chapters = chapters + loaded)
    }

    fun hasChapter(chapterId: String): Boolean = chapters.containsKey(chapterId)

    companion object {
        fun empty(): ChapterCache = ChapterCache(chapters = emptyMap())
    }
}
