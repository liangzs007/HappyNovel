package com.happynovel.admin

import com.happynovel.content.ChapterSummary
import com.happynovel.content.ContentRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminChapterRow(
    val id: String,
    val order: Int,
    val title: String,
    val crawlStatus: String,
    val cleanStatus: String,
    val translationStatus: String,
    val publishStatus: String,
    val updatedAt: String,
)

data class AdminChaptersResponse(
    val chapters: List<AdminChapterRow>,
    val emptyText: String,
)

@RestController
@RequestMapping("/api/admin/chapters")
class AdminChaptersController(
    private val contentRepository: ContentRepository,
) {
    @GetMapping
    fun chapters(
        @RequestParam(defaultValue = "book-seed-1") bookId: String,
    ): AdminChaptersResponse = AdminChaptersResponse(
        chapters = contentRepository.chapterCatalog(bookId).map(::toAdminChapterRow),
        emptyText = "暂无章节，请先触发书籍抓取。",
    )
}

private fun toAdminChapterRow(chapter: ChapterSummary): AdminChapterRow = AdminChapterRow(
    id = chapter.id,
    order = chapter.order,
    title = chapter.title,
    crawlStatus = "已抓取",
    cleanStatus = "已清洗",
    translationStatus = "已翻译",
    publishStatus = "已发布",
    updatedAt = chapter.updatedAt,
)
