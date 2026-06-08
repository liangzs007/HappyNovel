package com.happynovel.reader

import java.io.File

class FileReaderLocalDatabase(
    private val file: File,
) : ReaderLocalDatabase {
    private var state: ReaderLocalState = ReaderLocalStateCodec.decode(readState())

    override fun books(): List<ReaderBookRow> =
        state.bookshelf.savedBooks.map { it.toReaderBookRow() }

    override fun upsertBook(row: ReaderBookRow) {
        update(state.copy(bookshelf = state.bookshelf.saveBook(row.toBookSummary())))
    }

    override fun progressRows(): List<ReaderProgressRow> =
        state.bookshelf.progress.values.map { it.toReaderProgressRow() }

    override fun upsertProgress(row: ReaderProgressRow) {
        update(state.copy(bookshelf = state.bookshelf.updateProgress(row.toReadingProgress())))
    }

    override fun settings(): ReaderSettingsRow = state.settings.toReaderSettingsRow()

    override fun upsertSettings(row: ReaderSettingsRow) {
        update(state.copy(settings = row.toReaderSettings()))
    }

    override fun chapters(): List<ReaderChapterRow> =
        state.cache.chapters.values.map { it.toReaderChapterRow() }

    override fun upsertChapter(row: ReaderChapterRow) {
        update(state.copy(cache = state.cache.preload(row.toChapterContent())))
    }

    private fun update(newState: ReaderLocalState) {
        state = newState
        file.parentFile?.mkdirs()
        file.writeText(ReaderLocalStateCodec.encode(newState))
    }

    private fun readState(): String {
        if (!file.exists()) {
            return ""
        }
        return file.readText()
    }
}

fun BookSummary.toReaderBookRow(): ReaderBookRow = ReaderBookRow(
    id = id,
    title = title,
    latestChapterTitle = latestChapterTitle,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    updatedAt = updatedAt,
)

fun ReaderBookRow.toBookSummary(): BookSummary = BookSummary(
    id = id,
    title = title,
    latestChapterTitle = latestChapterTitle,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    updatedAt = updatedAt,
)

fun ReadingProgress.toReaderProgressRow(): ReaderProgressRow = ReaderProgressRow(
    bookId = bookId,
    chapterId = chapterId,
    percent = percent.coerceIn(0f, 1f),
)

fun ReaderProgressRow.toReadingProgress(): ReadingProgress = ReadingProgress(
    bookId = bookId,
    chapterId = chapterId,
    percent = percent,
)

fun ReaderSettings.toReaderSettingsRow(): ReaderSettingsRow = ReaderSettingsRow(
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    theme = theme.name,
    background = background.name,
)

fun ReaderSettingsRow.toReaderSettings(): ReaderSettings = ReaderSettings(
    fontSizeSp = fontSizeSp,
    lineHeightMultiplier = lineHeightMultiplier,
    theme = enumValueOf(theme),
    background = enumValueOf(background),
)

fun ChapterContent.toReaderChapterRow(): ReaderChapterRow = ReaderChapterRow(
    id = id,
    title = title,
    paragraphs = paragraphs,
)

fun ReaderChapterRow.toChapterContent(): ChapterContent = ChapterContent(
    id = id,
    title = title,
    paragraphs = paragraphs,
)
