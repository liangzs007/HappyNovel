package com.happynovel.content

data class Category(
    val id: String,
    val name: String,
    val slug: String,
)

data class BookSummary(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val latestChapterTitle: String,
    val updatedAt: String,
)

data class ChapterSummary(
    val id: String,
    val order: Int,
    val title: String,
    val updatedAt: String,
)

data class ChapterContent(
    val id: String,
    val bookId: String,
    val title: String,
    val language: String,
    val paragraphs: List<String>,
)

data class BookDetail(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val description: String,
    val status: String,
    val categories: List<Category>,
    val chapterCount: Int,
    val latestChapter: ChapterSummary?,
)

interface ContentRepository {
    fun homeBooks(): List<BookSummary>
    fun recommendedBooks(): List<BookSummary> = homeBooks()
    fun latestBooks(): List<BookSummary> = homeBooks()
    fun popularBooks(): List<BookSummary> = homeBooks()
    fun newBooks(): List<BookSummary> = homeBooks()
    fun categories(): List<Category>
    fun statuses(): List<String>
    fun bookDetail(bookId: String): BookDetail
    fun chapterCatalog(bookId: String): List<ChapterSummary>
    fun chapterContent(chapterId: String): ChapterContent
}
