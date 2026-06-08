package com.happynovel.admin

import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminChaptersControllerTest {
    @Test
    fun `admin chapters endpoint returns chapter rows for selected book`() {
        val controller = AdminChaptersController(AdminChaptersContentRepository())

        val response = controller.chapters(bookId = "book-seed-1")

        assertEquals("暂无章节，请先触发书籍抓取。", response.emptyText)
        assertEquals(1, response.chapters.size)
        assertEquals("chapter-seed-1", response.chapters.single().id)
        assertEquals(1, response.chapters.single().order)
        assertEquals("Chapter 1: Azure Cloud Sect", response.chapters.single().title)
        assertEquals("已抓取", response.chapters.single().crawlStatus)
        assertEquals("已清洗", response.chapters.single().cleanStatus)
        assertEquals("已翻译", response.chapters.single().translationStatus)
        assertEquals("已发布", response.chapters.single().publishStatus)
    }
}

private class AdminChaptersContentRepository : ContentRepository {
    override fun homeBooks(): List<BookSummary> = emptyList()

    override fun categories(): List<Category> = emptyList()

    override fun statuses(): List<String> = emptyList()

    override fun bookDetail(bookId: String): BookDetail = throw NoSuchElementException(bookId)

    override fun chapterCatalog(bookId: String): List<ChapterSummary> = listOf(
        ChapterSummary(
            id = "chapter-seed-1",
            order = 1,
            title = "Chapter 1: Azure Cloud Sect",
            updatedAt = "2026-06-08T00:00:00Z",
        ),
    )

    override fun chapterContent(chapterId: String): ChapterContent = throw NoSuchElementException(chapterId)
}
