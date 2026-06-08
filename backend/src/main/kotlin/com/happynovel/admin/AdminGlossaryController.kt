package com.happynovel.admin

import com.happynovel.translation.GlossaryService
import com.happynovel.translation.GlossaryTerm
import com.happynovel.translation.AddGlossaryTermRequest
import com.happynovel.translation.CreatePendingGlossaryTermRequest
import com.happynovel.translation.ConfirmPendingGlossaryTermRequest
import com.happynovel.translation.PendingGlossaryTerm
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminGlossaryTermRow(
    val id: String,
    val bookId: String,
    val sourceTerm: String,
    val translatedTerm: String,
    val type: String,
    val enabledStatus: String,
    val description: String,
)

data class AdminGlossaryResponse(
    val terms: List<AdminGlossaryTermRow>,
    val emptyText: String,
)

data class AdminPendingGlossaryTermRow(
    val id: String,
    val bookId: String,
    val chapterId: String?,
    val sourceTerm: String,
    val suggestedTranslation: String,
    val occurrenceCount: Int,
    val status: String,
)

data class AdminPendingGlossaryResponse(
    val pendingTerms: List<AdminPendingGlossaryTermRow>,
    val emptyText: String,
)

@RestController
@RequestMapping("/api/admin/glossary")
class AdminGlossaryController(
    private val glossaryService: GlossaryService,
) {
    @GetMapping
    fun terms(
        @RequestParam(required = false) bookId: String?,
    ): AdminGlossaryResponse = AdminGlossaryResponse(
        terms = glossaryService.terms(bookId).map(::toAdminGlossaryTermRow),
        emptyText = "暂无术语，请为书籍添加术语表。",
    )

    @PostMapping
    fun createTerm(@RequestBody request: AddGlossaryTermRequest): AdminGlossaryResponse {
        glossaryService.addTerm(request)
        return AdminGlossaryResponse(
            terms = glossaryService.terms(request.bookId).map(::toAdminGlossaryTermRow),
            emptyText = "暂无术语，请为书籍添加术语表。",
        )
    }

    @GetMapping("/pending")
    fun pendingTerms(
        @RequestParam(required = false) bookId: String?,
    ): AdminPendingGlossaryResponse = AdminPendingGlossaryResponse(
        pendingTerms = glossaryService.pendingTerms(bookId).map(::toAdminPendingGlossaryTermRow),
        emptyText = "暂无待确认术语。",
    )

    @PostMapping("/pending")
    fun createPendingTerm(@RequestBody request: CreatePendingGlossaryTermRequest): AdminPendingGlossaryResponse {
        glossaryService.createPendingTerm(request)
        return AdminPendingGlossaryResponse(
            pendingTerms = glossaryService.pendingTerms(request.bookId).map(::toAdminPendingGlossaryTermRow),
            emptyText = "暂无待确认术语。",
        )
    }

    @PostMapping("/pending/{id}/confirm")
    fun confirmPendingTerm(
        @org.springframework.web.bind.annotation.PathVariable id: String,
        @RequestBody request: ConfirmPendingGlossaryTermRequest,
    ): AdminPendingGlossaryResponse {
        val confirmed = glossaryService.confirmPendingTerm(id, request)
        return AdminPendingGlossaryResponse(
            pendingTerms = glossaryService.pendingTerms(confirmed.bookId).map(::toAdminPendingGlossaryTermRow),
            emptyText = "暂无待确认术语。",
        )
    }
}

private fun toAdminGlossaryTermRow(term: GlossaryTerm): AdminGlossaryTermRow = AdminGlossaryTermRow(
    id = term.id,
    bookId = term.bookId,
    sourceTerm = term.sourceTerm,
    translatedTerm = term.translatedTerm,
    type = term.type.name,
    enabledStatus = if (term.enabled) "启用" else "停用",
    description = term.description,
)

private fun toAdminPendingGlossaryTermRow(term: PendingGlossaryTerm): AdminPendingGlossaryTermRow =
    AdminPendingGlossaryTermRow(
        id = term.id,
        bookId = term.bookId,
        chapterId = term.chapterId,
        sourceTerm = term.sourceTerm,
        suggestedTranslation = term.suggestedTranslation ?: "-",
        occurrenceCount = term.occurrenceCount,
        status = term.status,
    )
