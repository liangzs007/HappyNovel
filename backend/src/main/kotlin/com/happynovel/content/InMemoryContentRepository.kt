package com.happynovel.content

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.beans.factory.ObjectProvider
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate

class InMemoryContentRepository private constructor(
    private val categories: List<Category>,
    private val books: List<BookSummary>,
    private val chaptersByBook: Map<String, List<ChapterSummary>>,
    private val contentByChapter: Map<String, ChapterContent>,
) : ContentRepository {
    override fun homeBooks(): List<BookSummary> = books

    override fun categories(): List<Category> = categories

    override fun statuses(): List<String> = listOf("ongoing", "completed")

    override fun bookDetail(bookId: String): BookDetail {
        val book = books.firstOrNull { it.id == bookId } ?: throw NoSuchElementException("Book not found: $bookId")
        val chapters = chapterCatalog(bookId)
        return BookDetail(
            id = book.id,
            title = book.title,
            author = book.author,
            coverUrl = book.coverUrl,
            description = book.description,
            status = book.status,
            categories = categories.take(1),
            chapterCount = chapters.size,
            latestChapter = chapters.maxByOrNull { it.order },
        )
    }

    override fun chapterCatalog(bookId: String): List<ChapterSummary> =
        chaptersByBook[bookId]?.sortedBy { it.order } ?: emptyList()

    override fun chapterContent(chapterId: String): ChapterContent =
        contentByChapter[chapterId] ?: throw NoSuchElementException("Chapter not found: $chapterId")

    companion object {
        fun withSeedData(): InMemoryContentRepository {
            val fantasy = Category(id = "category-fantasy", name = "Fantasy", slug = "fantasy")
            val book = BookSummary(
                id = "book-seed-1",
                title = "Dragon Gate",
                author = "Happy Novel Team",
                coverUrl = "https://example.com/covers/dragon-gate.jpg",
                description = "A translated cultivation novel prepared for MVP API validation.",
                status = "ongoing",
                latestChapterTitle = "Chapter 2: The Trial",
                updatedAt = "2026-06-08T00:00:00Z",
            )
            val chapters = listOf(
                ChapterSummary("chapter-seed-1", 1, "Chapter 1: Azure Cloud Sect", "2026-06-08T00:00:00Z"),
                ChapterSummary("chapter-seed-2", 2, "Chapter 2: The Trial", "2026-06-08T00:00:00Z"),
            )
            val contents = mapOf(
                "chapter-seed-1" to ChapterContent(
                    id = "chapter-seed-1",
                    bookId = "book-seed-1",
                    title = "Chapter 1: Azure Cloud Sect",
                    language = "en",
                    paragraphs = listOf(
                        "The morning bell echoed across Azure Cloud Sect.",
                        "Lin Chen stepped through the Dragon Gate for the first time.",
                    ),
                ),
                "chapter-seed-2" to ChapterContent(
                    id = "chapter-seed-2",
                    bookId = "book-seed-1",
                    title = "Chapter 2: The Trial",
                    language = "en",
                    paragraphs = listOf("The first trial began before sunrise."),
                ),
            )
            return InMemoryContentRepository(
                categories = listOf(fantasy),
                books = listOf(book),
                chaptersByBook = mapOf(book.id to chapters),
                contentByChapter = contents,
            )
        }
    }
}

@Configuration
class ContentConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun contentRepository(): ContentRepository {
        val mode = environment.getProperty("app.content.repository-mode", "SEED")
            .uppercase()
            .let(ContentRepositoryMode::valueOf)
        val databaseClient = jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
        return ContentRepositoryFactory.create(
            mode = mode,
            databaseClient = databaseClient,
        )
    }
}
