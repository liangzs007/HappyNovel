package com.happynovel.app

import com.happynovel.admin.InMemoryCompliancePolicyService
import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import com.happynovel.publication.InMemoryPublicationControlService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AppApiControllerSectionsTest {
    private val controller = AppApiController(
        FakeSectionContentRepository(),
        InMemoryCompliancePolicyService(),
        InMemoryPublicationControlService(),
    )

    @Test
    fun `home endpoint uses distinct repository sections`() {
        val response = controller.home()

        assertEquals("recommended-book", response.recommended.single().id)
        assertEquals("latest-book", response.latestUpdates.single().id)
        assertEquals("popular-book", response.popular.single().id)
        assertEquals("new-book", response.newBooks.single().id)
    }
}

private class FakeSectionContentRepository : ContentRepository {
    override fun homeBooks(): List<BookSummary> = listOf(book("fallback-book"))

    override fun categories(): List<Category> = listOf(Category("category-fantasy", "Fantasy", "fantasy"))

    override fun statuses(): List<String> = listOf("ongoing", "completed")

    override fun bookDetail(bookId: String): BookDetail = BookDetail(
        id = bookId,
        title = "Dragon Gate",
        author = "Happy Novel Team",
        coverUrl = "",
        description = "",
        status = "ongoing",
        categories = categories(),
        chapterCount = 1,
        latestChapter = ChapterSummary("chapter-seed-1", 1, "Chapter 1", "2026-06-08T00:00:00Z"),
    )

    override fun chapterCatalog(bookId: String): List<ChapterSummary> = listOf(
        ChapterSummary("chapter-seed-1", 1, "Chapter 1", "2026-06-08T00:00:00Z"),
    )

    override fun chapterContent(chapterId: String): ChapterContent = ChapterContent(
        id = chapterId,
        bookId = "book-seed-1",
        title = "Chapter 1",
        language = "en",
        paragraphs = listOf("Paragraph"),
    )

    override fun recommendedBooks(): List<BookSummary> = listOf(book("recommended-book"))

    override fun latestBooks(): List<BookSummary> = listOf(book("latest-book"))

    override fun popularBooks(): List<BookSummary> = listOf(book("popular-book"))

    override fun newBooks(): List<BookSummary> = listOf(book("new-book"))

    private fun book(id: String) = BookSummary(
        id = id,
        title = id,
        author = "Author",
        coverUrl = "",
        description = "",
        status = "ongoing",
        latestChapterTitle = "Chapter 1",
        updatedAt = "2026-06-08T00:00:00Z",
    )
}
