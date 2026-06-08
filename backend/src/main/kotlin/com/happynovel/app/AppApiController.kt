package com.happynovel.app

import com.happynovel.content.BookDetail
import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ChapterContent
import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class HomeResponse(
    val appName: String,
    val recommended: List<BookSummary>,
    val latestUpdates: List<BookSummary>,
    val popular: List<BookSummary>,
    val newBooks: List<BookSummary>,
)

data class CategoriesResponse(
    val categories: List<Category>,
    val statuses: List<String>,
)

data class ChapterCatalogResponse(
    val bookId: String,
    val chapters: List<ChapterSummary>,
)

data class AnonymousDeviceResponse(
    val deviceId: String,
)

data class AdConfigResponse(
    val enabled: Boolean,
    val readerBannerEnabled: Boolean,
    val interstitialEveryChapters: Int,
)

@RestController
@RequestMapping("/api/app")
class AppApiController(
    private val contentRepository: ContentRepository,
) {
    @GetMapping("/home")
    fun home(): HomeResponse {
        val books = contentRepository.homeBooks()
        return HomeResponse(
            appName = "HappyNovel",
            recommended = books,
            latestUpdates = books,
            popular = books,
            newBooks = books,
        )
    }

    @GetMapping("/categories")
    fun categories(): CategoriesResponse = CategoriesResponse(
        categories = contentRepository.categories(),
        statuses = contentRepository.statuses(),
    )

    @GetMapping("/books/{bookId}")
    fun bookDetail(@PathVariable bookId: String): BookDetail = contentRepository.bookDetail(bookId)

    @GetMapping("/books/{bookId}/chapters")
    fun chapterCatalog(@PathVariable bookId: String): ChapterCatalogResponse = ChapterCatalogResponse(
        bookId = bookId,
        chapters = contentRepository.chapterCatalog(bookId),
    )

    @GetMapping("/chapters/{chapterId}")
    fun chapterContent(@PathVariable chapterId: String): ChapterContent = contentRepository.chapterContent(chapterId)

    @PostMapping("/devices/anonymous")
    fun createAnonymousDevice(): AnonymousDeviceResponse = AnonymousDeviceResponse(
        deviceId = "anon-${UUID.randomUUID()}",
    )

    @GetMapping("/ad-config")
    fun adConfig(): AdConfigResponse = AdConfigResponse(
        enabled = true,
        readerBannerEnabled = true,
        interstitialEveryChapters = 5,
    )
}
