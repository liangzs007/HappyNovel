package com.happynovel.admin

import com.happynovel.content.BookSummary
import com.happynovel.content.ContentRepository
import com.happynovel.publication.PublicationControlService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminBooksResponse(
    val books: List<BookSummary>,
    val emptyText: String,
)

data class BookPublicationResponse(
    val bookId: String,
    val publishStatus: String,
)

@RestController
@RequestMapping("/api/admin/books")
class AdminBooksController(
    private val contentRepository: ContentRepository,
    private val publicationControlService: PublicationControlService,
) {
    @GetMapping
    fun books(
        @RequestParam(defaultValue = "50") limit: Int,
    ): AdminBooksResponse = AdminBooksResponse(
        books = contentRepository.browseBooks(
            category = null,
            status = null,
            sort = "latest",
            limit = limit.coerceIn(1, 100),
        ),
        emptyText = "暂无书籍，请添加小说来源 URL。",
    )

    @PostMapping("/{bookId}/unpublish")
    fun unpublishBook(@PathVariable bookId: String): BookPublicationResponse {
        publicationControlService.unpublishBook(bookId)
        return BookPublicationResponse(bookId = bookId, publishStatus = "已下架")
    }
}
