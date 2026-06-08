package com.happynovel.admin

import com.happynovel.content.BookSummary
import com.happynovel.content.Category
import com.happynovel.content.ContentRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminRecommendationRow(
    val id: String,
    val name: String,
    val type: String,
    val boundBook: String,
    val sortWeight: String,
    val enabledStatus: String,
)

data class AdminRecommendationsResponse(
    val items: List<AdminRecommendationRow>,
    val emptyText: String,
)

@RestController
@RequestMapping("/api/admin/recommendations")
class AdminRecommendationsController(
    private val contentRepository: ContentRepository,
) {
    @GetMapping
    fun recommendations(): AdminRecommendationsResponse = AdminRecommendationsResponse(
        items = contentRepository.categories().map(::categoryRow) +
            contentRepository.recommendedBooks().mapIndexed(::recommendedBookRow),
        emptyText = "暂无推荐配置。",
    )
}

private fun categoryRow(category: Category): AdminRecommendationRow = AdminRecommendationRow(
    id = category.id,
    name = category.name,
    type = "分类",
    boundBook = "-",
    sortWeight = "0",
    enabledStatus = "启用",
)

private fun recommendedBookRow(index: Int, book: BookSummary): AdminRecommendationRow = AdminRecommendationRow(
    id = "recommendation-${book.id}",
    name = book.title,
    type = "推荐书籍",
    boundBook = book.title,
    sortWeight = (index + 1).toString(),
    enabledStatus = "启用",
)
