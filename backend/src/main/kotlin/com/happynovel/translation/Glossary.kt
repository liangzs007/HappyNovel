package com.happynovel.translation

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.UUID

enum class GlossaryTermType {
    PERSON,
    PLACE,
    ORGANIZATION,
    SKILL,
    ITEM,
    TITLE,
    OTHER,
}

data class AddGlossaryTermRequest(
    val bookId: String,
    val sourceTerm: String,
    val translatedTerm: String,
    val type: GlossaryTermType,
    val description: String,
)

data class GlossaryTerm(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val sourceTerm: String,
    val translatedTerm: String,
    val type: GlossaryTermType,
    val description: String,
    val enabled: Boolean = true,
)

interface GlossaryService {
    fun addTerm(request: AddGlossaryTermRequest): GlossaryTerm
    fun enabledTerms(bookId: String): List<GlossaryTerm>
    fun terms(bookId: String? = null): List<GlossaryTerm>
}

class InMemoryGlossaryService : GlossaryService {
    private val storedTerms = mutableListOf<GlossaryTerm>()

    override fun addTerm(request: AddGlossaryTermRequest): GlossaryTerm {
        val term = GlossaryTerm(
            bookId = request.bookId,
            sourceTerm = request.sourceTerm,
            translatedTerm = request.translatedTerm,
            type = request.type,
            description = request.description,
        )
        storedTerms += term
        return term
    }

    override fun enabledTerms(bookId: String): List<GlossaryTerm> =
        storedTerms.filter { it.bookId == bookId && it.enabled }

    override fun terms(bookId: String?): List<GlossaryTerm> =
        storedTerms.filter { bookId.isNullOrBlank() || it.bookId == bookId }
}

@Configuration
class GlossaryConfiguration {
    @Bean
    fun glossaryService(): GlossaryService = InMemoryGlossaryService()
}
