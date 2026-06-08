package com.happynovel.app

import com.happynovel.admin.InMemoryAdConfigService
import com.happynovel.admin.InMemoryCompliancePolicyService
import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import com.happynovel.publication.InMemoryPublicationControlService
import com.happynovel.reading.InMemoryReadingEventService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AppApiBookListTest {
    @Test
    fun `book list endpoint passes category status sort and limit to repository`() {
        val repository = RecordingBrowseContentRepository()
        val controller = AppApiController(
            repository,
            InMemoryAdConfigService(),
            InMemoryCompliancePolicyService(),
            InMemoryPublicationControlService(),
            InMemoryReadingEventService(),
        )

        val response = controller.books(
            category = "fantasy",
            status = "ongoing",
            sort = "popular",
            limit = 12,
        )

        assertEquals("book-filtered", response.books.single().id)
        assertEquals(BrowseCall("fantasy", "ongoing", "popular", 12), repository.calls.single())
    }
}

private data class BrowseCall(
    val category: String?,
    val status: String?,
    val sort: String?,
    val limit: Int,
)

private class RecordingBrowseContentRepository : ContentRepository {
    val calls = mutableListOf<BrowseCall>()

    override fun homeBooks(): List<BookSummary> = emptyList()

    override fun browseBooks(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): List<BookSummary> {
        calls.add(BrowseCall(category, status, sort, limit))
        return listOf(book("book-filtered"))
    }

    override fun categories(): List<Category> = emptyList()

    override fun statuses(): List<String> = emptyList()

    override fun bookDetail(bookId: String): BookDetail = throw NoSuchElementException(bookId)

    override fun chapterCatalog(bookId: String): List<ChapterSummary> = emptyList()

    override fun chapterContent(chapterId: String): ChapterContent = throw NoSuchElementException(chapterId)

    private fun book(id: String): BookSummary = BookSummary(
        id = id,
        title = "Dragon Gate",
        author = "Happy Novel Team",
        coverUrl = "",
        description = "",
        status = "ongoing",
        latestChapterTitle = "Chapter 1",
        updatedAt = "2026-06-08T00:00:00Z",
    )
}
