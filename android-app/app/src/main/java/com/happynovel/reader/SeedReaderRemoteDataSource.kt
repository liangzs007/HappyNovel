package com.happynovel.reader

class SeedReaderRemoteDataSource : ReaderRemoteDataSource {
    override fun home(): AppHomeResponseDto = AppHomeResponseDto(
        appName = "HappyNovel",
        recommended = listOf(book),
        latestUpdates = listOf(book),
        popular = listOf(book),
        newBooks = listOf(book),
    )

    override fun categories(): AppCategoriesResponseDto = AppCategoriesResponseDto(
        categories = listOf(category),
        statuses = listOf("ongoing", "completed"),
    )

    override fun bookDetail(bookId: String): AppBookDetailDto = AppBookDetailDto(
        id = book.id,
        title = book.title,
        author = book.author,
        coverUrl = book.coverUrl,
        description = book.description,
        status = book.status,
        categories = listOf(category),
        chapterCount = chapters.size,
        latestChapter = chapters.maxByOrNull { it.order },
    )

    override fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto = AppChapterCatalogResponseDto(
        bookId = bookId,
        chapters = chapters,
    )

    override fun chapterContent(chapterId: String): AppChapterContentDto = AppChapterContentDto(
        id = chapterId,
        bookId = book.id,
        title = "Chapter 1: Azure Cloud Sect",
        language = "en",
        paragraphs = listOf(
            "The morning bell echoed across Azure Cloud Sect.",
            "Lin Chen stepped through the Dragon Gate for the first time.",
        ),
    )

    private val category = AppCategoryDto(
        id = "category-fantasy",
        name = "Fantasy",
        slug = "fantasy",
    )

    private val book = AppBookSummaryDto(
        id = "book-seed-1",
        title = "Dragon Gate",
        author = "Happy Novel Team",
        coverUrl = "https://example.com/covers/dragon-gate.jpg",
        description = "A translated cultivation novel prepared for MVP API validation.",
        status = "ongoing",
        latestChapterTitle = "Chapter 1: Azure Cloud Sect",
        updatedAt = "2026-06-08T00:00:00Z",
    )

    private val chapters = listOf(
        AppChapterSummaryDto("chapter-seed-1", 1, "Chapter 1: Azure Cloud Sect", "2026-06-08T00:00:00Z"),
    )
}
