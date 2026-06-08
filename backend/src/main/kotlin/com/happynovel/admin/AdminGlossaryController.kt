package com.happynovel.admin

import com.happynovel.translation.GlossaryService
import com.happynovel.translation.GlossaryTerm
import com.happynovel.translation.AddGlossaryTermRequest
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
