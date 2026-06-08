package com.happynovel.reader

class HappyNovelApiRoutes(baseUrl: String) {
    private val normalizedBaseUrl = baseUrl.trimEnd('/')

    fun home(): String = "$normalizedBaseUrl/api/app/home"

    fun categories(): String = "$normalizedBaseUrl/api/app/categories"

    fun bookDetail(bookId: String): String = "$normalizedBaseUrl/api/app/books/$bookId"

    fun chapterCatalog(bookId: String): String = "$normalizedBaseUrl/api/app/books/$bookId/chapters"

    fun chapterContent(chapterId: String): String = "$normalizedBaseUrl/api/app/chapters/$chapterId"

    fun adConfig(): String = "$normalizedBaseUrl/api/app/ad-config"
}

data class HomeState(
    val appName: String,
    val recommended: List<BookSummary>,
    val latestUpdates: List<BookSummary>,
    val popular: List<BookSummary>,
    val newBooks: List<BookSummary>,
)

data class CategorySummary(
    val id: String,
    val name: String,
    val slug: String,
)

data class ChapterSummary(
    val id: String,
    val order: Int,
    val title: String,
    val updatedAt: String,
)

data class BookDetailState(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val categories: List<CategorySummary>,
    val chapterCount: Int,
    val latestChapter: ChapterSummary?,
)

data class AppHomeResponseDto(
    val appName: String,
    val recommended: List<AppBookSummaryDto>,
    val latestUpdates: List<AppBookSummaryDto>,
    val popular: List<AppBookSummaryDto>,
    val newBooks: List<AppBookSummaryDto>,
)

data class AppCategoriesResponseDto(
    val categories: List<AppCategoryDto>,
    val statuses: List<String>,
)

data class AppCategoryDto(
    val id: String,
    val name: String,
    val slug: String,
)

data class AppBookSummaryDto(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val latestChapterTitle: String,
    val updatedAt: String,
)

data class AppChapterSummaryDto(
    val id: String,
    val order: Int,
    val title: String,
    val updatedAt: String,
)

data class AppBookDetailDto(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val categories: List<AppCategoryDto>,
    val chapterCount: Int,
    val latestChapter: AppChapterSummaryDto?,
)

data class AppChapterCatalogResponseDto(
    val bookId: String,
    val chapters: List<AppChapterSummaryDto>,
)

data class AppChapterContentDto(
    val id: String,
    val bookId: String,
    val title: String,
    val language: String,
    val paragraphs: List<String>,
)

data class AppAdConfigDto(
    val enabled: Boolean,
    val readerBannerEnabled: Boolean,
    val interstitialEveryChapters: Int,
)

fun AppHomeResponseDto.toHomeState(): HomeState = HomeState(
    appName = appName,
    recommended = recommended.map { it.toReaderBookSummary() },
    latestUpdates = latestUpdates.map { it.toReaderBookSummary() },
    popular = popular.map { it.toReaderBookSummary() },
    newBooks = newBooks.map { it.toReaderBookSummary() },
)

fun AppBookSummaryDto.toReaderBookSummary(): BookSummary = BookSummary(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    latestChapterTitle = latestChapterTitle,
    updatedAt = updatedAt,
)

fun AppCategoryDto.toCategorySummary(): CategorySummary = CategorySummary(
    id = id,
    name = name,
    slug = slug,
)

fun AppChapterSummaryDto.toChapterSummary(): ChapterSummary = ChapterSummary(
    id = id,
    order = order,
    title = title,
    updatedAt = updatedAt,
)

fun AppBookDetailDto.toBookDetailState(): BookDetailState = BookDetailState(
    id = id,
    title = title,
    author = author,
    coverUrl = coverUrl,
    description = description,
    status = status,
    categories = categories.map { it.toCategorySummary() },
    chapterCount = chapterCount,
    latestChapter = latestChapter?.toChapterSummary(),
)

fun AppChapterContentDto.toReaderChapter(): ChapterContent = ChapterContent(
    id = id,
    title = title,
    paragraphs = paragraphs,
)
