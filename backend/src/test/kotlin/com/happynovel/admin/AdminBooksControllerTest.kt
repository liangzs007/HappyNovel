package com.happynovel.admin

import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminBooksControllerTest {
    @Test
    fun `admin books endpoint returns table rows from content repository`() {
        val controller = AdminBooksController(AdminBooksContentRepository())

        val response = controller.books(limit = 20)

        assertEquals("暂无书籍，请添加小说来源 URL。", response.emptyText)
        assertEquals(1, response.books.size)
        assertEquals("book-admin-1", response.books.single().id)
        assertEquals("Dragon Gate", response.books.single().title)
        assertEquals("ongoing", response.books.single().status)
        assertEquals("Chapter 2: The Trial", response.books.single().latestChapterTitle)
    }
}

private class AdminBooksContentRepository : ContentRepository {
    override fun homeBooks(): List<BookSummary> = listOf(
        BookSummary(
            id = "book-admin-1",
            title = "Dragon Gate",
            author = "Happy Novel Team",
            coverUrl = "",
            description = "A translated cultivation novel.",
            status = "ongoing",
            latestChapterTitle = "Chapter 2: The Trial",
            updatedAt = "2026-06-08T00:00:00Z",
        ),
    )

    override fun categories(): List<Category> = emptyList()

    override fun statuses(): List<String> = emptyList()

    override fun bookDetail(bookId: String): BookDetail = throw NoSuchElementException(bookId)

    override fun chapterCatalog(bookId: String): List<ChapterSummary> = emptyList()

    override fun chapterContent(chapterId: String): ChapterContent = throw NoSuchElementException(chapterId)
}
