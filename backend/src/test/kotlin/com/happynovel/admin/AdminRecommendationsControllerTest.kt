package com.happynovel.admin

import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminRecommendationsControllerTest {
    @Test
    fun `admin recommendations endpoint returns category and recommended book rows`() {
        val controller = AdminRecommendationsController(AdminRecommendationsContentRepository())

        val response = controller.recommendations()

        assertEquals("暂无推荐配置。", response.emptyText)
        assertEquals(2, response.items.size)
        assertEquals("Fantasy", response.items.first().name)
        assertEquals("分类", response.items.first().type)
        assertEquals("Dragon Gate", response.items.last().name)
        assertEquals("推荐书籍", response.items.last().type)
        assertEquals("启用", response.items.last().enabledStatus)
    }
}

private class AdminRecommendationsContentRepository : ContentRepository {
    override fun homeBooks(): List<BookSummary> = emptyList()

    override fun recommendedBooks(): List<BookSummary> = listOf(
        BookSummary(
            id = "book-seed-1",
            title = "Dragon Gate",
            author = "Happy Novel Team",
            coverUrl = "",
            description = "A translated cultivation novel.",
            status = "ongoing",
            latestChapterTitle = "Chapter 2: The Trial",
            updatedAt = "2026-06-08T00:00:00Z",
        ),
    )

    override fun categories(): List<Category> = listOf(Category("category-fantasy", "Fantasy", "fantasy"))

    override fun statuses(): List<String> = emptyList()

    override fun bookDetail(bookId: String): BookDetail = throw NoSuchElementException(bookId)

    override fun chapterCatalog(bookId: String): List<ChapterSummary> = emptyList()

    override fun chapterContent(chapterId: String): ChapterContent = throw NoSuchElementException(chapterId)
}
