package com.happynovel.reader

import org.json.JSONArray
import org.json.JSONObject

data class ReaderLocalState(
    val bookshelf: BookshelfState,
    val settings: ReaderSettings,
    val cache: ChapterCache,
) {
    companion object {
        fun default(): ReaderLocalState = ReaderLocalState(
            bookshelf = BookshelfState.empty(),
            settings = ReaderSettings.default(),
            cache = ChapterCache.empty(),
        )
    }
}

object ReaderLocalStateCodec {
    fun encode(state: ReaderLocalState): String = JSONObject()
        .put("bookshelf", state.bookshelf.toJson())
        .put("settings", state.settings.toJson())
        .put("cache", state.cache.toJson())
        .toString()

    fun decode(json: String): ReaderLocalState {
        if (json.isBlank()) {
            return ReaderLocalState.default()
        }
        val root = JSONObject(json)
        return ReaderLocalState(
            bookshelf = root.optJSONObject("bookshelf")?.toBookshelfState() ?: BookshelfState.empty(),
            settings = root.optJSONObject("settings")?.toReaderSettings() ?: ReaderSettings.default(),
            cache = root.optJSONObject("cache")?.toChapterCache() ?: ChapterCache.empty(),
        )
    }

    private fun BookshelfState.toJson(): JSONObject = JSONObject()
        .put(
            "savedBooks",
            JSONArray(savedBooks.map { it.toJson() }),
        )
        .put(
            "progress",
            JSONArray(progress.values.map { it.toJson() }),
        )

    private fun BookSummary.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("latestChapterTitle", latestChapterTitle)
        .put("author", author)
        .put("coverUrl", coverUrl)
        .put("description", description)
        .put("status", status)
        .put("updatedAt", updatedAt)

    private fun ReadingProgress.toJson(): JSONObject = JSONObject()
        .put("bookId", bookId)
        .put("chapterId", chapterId)
        .put("percent", percent)

    private fun ReaderSettings.toJson(): JSONObject = JSONObject()
        .put("fontSizeSp", fontSizeSp)
        .put("lineHeightMultiplier", lineHeightMultiplier)
        .put("theme", theme.name)
        .put("background", background.name)

    private fun ChapterCache.toJson(): JSONObject = JSONObject()
        .put(
            "chapters",
            JSONArray(chapters.values.map { it.toJson() }),
        )

    private fun ChapterContent.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("title", title)
        .put("paragraphs", JSONArray(paragraphs))

    private fun JSONObject.toBookshelfState(): BookshelfState {
        val books = optJSONArray("savedBooks").mapObjects { it.toBookSummary() }
        val progress = optJSONArray("progress")
            .mapObjects { it.toReadingProgress() }
            .associateBy { it.bookId }
        return BookshelfState(books, progress)
    }

    private fun JSONObject.toBookSummary(): BookSummary = BookSummary(
        id = getString("id"),
        title = getString("title"),
        latestChapterTitle = getString("latestChapterTitle"),
        author = optString("author"),
        coverUrl = optString("coverUrl"),
        description = optString("description"),
        status = optString("status"),
        updatedAt = optString("updatedAt"),
    )

    private fun JSONObject.toReadingProgress(): ReadingProgress = ReadingProgress(
        bookId = getString("bookId"),
        chapterId = getString("chapterId"),
        percent = getDouble("percent").toFloat(),
    )

    private fun JSONObject.toReaderSettings(): ReaderSettings = ReaderSettings(
        fontSizeSp = optInt("fontSizeSp", ReaderSettings.default().fontSizeSp),
        lineHeightMultiplier = optDouble(
            "lineHeightMultiplier",
            ReaderSettings.default().lineHeightMultiplier.toDouble(),
        ).toFloat(),
        theme = enumValueOf(optString("theme", ReaderTheme.LIGHT.name)),
        background = enumValueOf(optString("background", ReaderBackground.DEFAULT.name)),
    )

    private fun JSONObject.toChapterCache(): ChapterCache {
        val chapters = optJSONArray("chapters")
            .mapObjects { it.toChapterContent() }
            .associateBy { it.id }
        return ChapterCache(chapters)
    }

    private fun JSONObject.toChapterContent(): ChapterContent = ChapterContent(
        id = getString("id"),
        title = getString("title"),
        paragraphs = optJSONArray("paragraphs").mapStrings(),
    )

    private fun JSONArray?.mapStrings(): List<String> {
        if (this == null) {
            return emptyList()
        }
        return List(length()) { index -> getString(index) }
    }

    private fun <T> JSONArray?.mapObjects(transform: (JSONObject) -> T): List<T> {
        if (this == null) {
            return emptyList()
        }
        return List(length()) { index -> transform(getJSONObject(index)) }
    }
}
