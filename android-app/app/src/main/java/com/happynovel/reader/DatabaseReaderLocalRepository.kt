package com.happynovel.reader

data class ReaderBookRow(
    val id: String,
    val title: String,
    val latestChapterTitle: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val updatedAt: String,
)

data class ReaderProgressRow(
    val bookId: String,
    val chapterId: String,
    val percent: Float,
)

data class ReaderSettingsRow(
    val fontSizeSp: Int,
    val lineHeightMultiplier: Float,
    val theme: String,
    val background: String,
)

data class ReaderChapterRow(
    val id: String,
    val title: String,
    val paragraphs: List<String>,
)

interface ReaderLocalDatabase {
    fun books(): List<ReaderBookRow>

    fun upsertBook(row: ReaderBookRow)

    fun progressRows(): List<ReaderProgressRow>

    fun upsertProgress(row: ReaderProgressRow)

    fun settings(): ReaderSettingsRow?

    fun upsertSettings(row: ReaderSettingsRow)

    fun chapters(): List<ReaderChapterRow>

    fun upsertChapter(row: ReaderChapterRow)
}

class InMemoryReaderLocalDatabase : ReaderLocalDatabase {
    private val books = linkedMapOf<String, ReaderBookRow>()
    private val progress = linkedMapOf<String, ReaderProgressRow>()
    private val chapters = linkedMapOf<String, ReaderChapterRow>()
    private var settings: ReaderSettingsRow? = null

    override fun books(): List<ReaderBookRow> = books.values.toList()

    override fun upsertBook(row: ReaderBookRow) {
        books[row.id] = row
    }

    override fun progressRows(): List<ReaderProgressRow> = progress.values.toList()

    override fun upsertProgress(row: ReaderProgressRow) {
        progress[row.bookId] = row
    }

    override fun settings(): ReaderSettingsRow? = settings

    override fun upsertSettings(row: ReaderSettingsRow) {
        settings = row
    }

    override fun chapters(): List<ReaderChapterRow> = chapters.values.toList()

    override fun upsertChapter(row: ReaderChapterRow) {
        chapters[row.id] = row
    }
}

class DatabaseReaderLocalRepository(
    private val database: ReaderLocalDatabase,
) : ReaderLocalRepository {
    override fun bookshelf(): BookshelfState = BookshelfState(
        savedBooks = database.books().map { it.toBookSummary() },
        progress = database.progressRows()
            .map { it.toReadingProgress() }
            .associateBy { it.bookId },
    )

    override fun saveBook(book: BookSummary) {
        database.upsertBook(book.toReaderBookRow())
    }

    override fun updateProgress(progress: ReadingProgress) {
        database.upsertProgress(progress.toReaderProgressRow())
    }

    override fun settings(): ReaderSettings =
        database.settings()?.toReaderSettings() ?: ReaderSettings.default()

    override fun updateSettings(settings: ReaderSettings) {
        database.upsertSettings(settings.toReaderSettingsRow())
    }

    override fun chapterCache(): ChapterCache = ChapterCache(
        chapters = database.chapters()
            .map { it.toChapterContent() }
            .associateBy { it.id },
    )

    override fun cacheChapter(chapter: ChapterContent) {
        database.upsertChapter(chapter.toReaderChapterRow())
    }

    override fun cachedChapter(chapterId: String): ChapterContent? = chapterCache().chapters[chapterId]
}

private fun BookSummary.toReaderBookRow(): ReaderBookRow = ReaderBookRow(
    id = id,
    title = title,
    latestChapterTitle = latestChapterTitle,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    updatedAt = updatedAt,
)

private fun ReaderBookRow.toBookSummary(): BookSummary = BookSummary(
    id = id,
    title = title,
    latestChapterTitle = latestChapterTitle,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    updatedAt = updatedAt,
)

private fun ReadingProgress.toReaderProgressRow(): ReaderProgressRow = ReaderProgressRow(
    bookId = bookId,
    chapterId = chapterId,
    percent = percent.coerceIn(0f, 1f),
)

private fun ReaderProgressRow.toReadingProgress(): ReadingProgress = ReadingProgress(
    bookId = bookId,
    chapterId = chapterId,
    percent = percent,
)

private fun ReaderSettings.toReaderSettingsRow(): ReaderSettingsRow = ReaderSettingsRow(
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    theme = theme.name,
    background = background.name,
)

private fun ReaderSettingsRow.toReaderSettings(): ReaderSettings = ReaderSettings(
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    theme = enumValueOf(theme),
    background = enumValueOf(background),
)

private fun ChapterContent.toReaderChapterRow(): ReaderChapterRow = ReaderChapterRow(
    id = id,
    title = title,
    paragraphs = paragraphs,
)

private fun ReaderChapterRow.toChapterContent(): ChapterContent = ChapterContent(
    id = id,
    title = title,
    paragraphs = paragraphs,
)
