package com.happynovel.translation

import com.happynovel.admin.booleanValue
import com.happynovel.admin.intValue
import com.happynovel.admin.stringValue
import com.happynovel.content.ContentDatabaseClient
import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
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

data class CreatePendingGlossaryTermRequest(
    val bookId: String,
    val chapterId: String?,
    val sourceTerm: String,
    val suggestedTranslation: String?,
    val occurrenceCount: Int,
)

data class ConfirmPendingGlossaryTermRequest(
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

data class PendingGlossaryTerm(
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val chapterId: String?,
    val sourceTerm: String,
    val suggestedTranslation: String?,
    val occurrenceCount: Int,
    val status: String = "PENDING",
)

interface GlossaryService {
    fun addTerm(request: AddGlossaryTermRequest): GlossaryTerm
    fun enabledTerms(bookId: String): List<GlossaryTerm>
    fun terms(bookId: String? = null): List<GlossaryTerm>
    fun createPendingTerm(request: CreatePendingGlossaryTermRequest): PendingGlossaryTerm
    fun pendingTerms(bookId: String? = null): List<PendingGlossaryTerm>
    fun confirmPendingTerm(id: String, request: ConfirmPendingGlossaryTermRequest): GlossaryTerm
}

class InMemoryGlossaryService : GlossaryService {
    private val storedTerms = mutableListOf<GlossaryTerm>()
    private val storedPendingTerms = mutableListOf<PendingGlossaryTerm>()

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

    override fun createPendingTerm(request: CreatePendingGlossaryTermRequest): PendingGlossaryTerm {
        val term = PendingGlossaryTerm(
            bookId = request.bookId,
            chapterId = request.chapterId,
            sourceTerm = request.sourceTerm,
            suggestedTranslation = request.suggestedTranslation,
            occurrenceCount = request.occurrenceCount.coerceAtLeast(1),
        )
        storedPendingTerms += term
        return term
    }

    override fun pendingTerms(bookId: String?): List<PendingGlossaryTerm> =
        storedPendingTerms.filter { it.status == "PENDING" }
            .filter { bookId.isNullOrBlank() || it.bookId == bookId }

    override fun confirmPendingTerm(id: String, request: ConfirmPendingGlossaryTermRequest): GlossaryTerm {
        val pending = storedPendingTerms.first { it.id == id }
        storedPendingTerms.replaceAll {
            if (it.id == id) it.copy(status = "CONFIRMED") else it
        }
        return addTerm(
            AddGlossaryTermRequest(
                bookId = pending.bookId,
                sourceTerm = pending.sourceTerm,
                translatedTerm = request.translatedTerm,
                type = request.type,
                description = request.description,
            ),
        )
    }
}

class JdbcGlossaryService(
    private val databaseClient: ContentDatabaseClient,
) : GlossaryService {
    override fun addTerm(request: AddGlossaryTermRequest): GlossaryTerm {
        val term = GlossaryTerm(
            bookId = request.bookId,
            sourceTerm = request.sourceTerm,
            translatedTerm = request.translatedTerm,
            type = request.type,
            description = request.description,
        )
        databaseClient.update(
            """
                insert into glossary_term(
                    id,
                    book_id,
                    source_term,
                    translated_term,
                    term_type,
                    description,
                    enabled
                )
                values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString(term.id),
            UUID.fromString(term.bookId),
            term.sourceTerm,
            term.translatedTerm,
            term.type.name,
            term.description,
            term.enabled,
        )
        return term
    }

    override fun enabledTerms(bookId: String): List<GlossaryTerm> =
        databaseClient.query(
            "$TERMS_SQL where book_id = ? and enabled = true order by source_term",
            UUID.fromString(bookId),
        ).map(::mapTerm)

    override fun terms(bookId: String?): List<GlossaryTerm> {
        val sql = if (bookId.isNullOrBlank()) {
            "$TERMS_SQL order by source_term"
        } else {
            "$TERMS_SQL where book_id = ? order by source_term"
        }
        val args = if (bookId.isNullOrBlank()) emptyArray() else arrayOf(UUID.fromString(bookId))
        return databaseClient.query(sql, *args).map(::mapTerm)
    }

    override fun createPendingTerm(request: CreatePendingGlossaryTermRequest): PendingGlossaryTerm {
        val term = PendingGlossaryTerm(
            bookId = request.bookId,
            chapterId = request.chapterId,
            sourceTerm = request.sourceTerm,
            suggestedTranslation = request.suggestedTranslation,
            occurrenceCount = request.occurrenceCount.coerceAtLeast(1),
        )
        databaseClient.update(
            """
                insert into pending_glossary_term(
                    id,
                    book_id,
                    chapter_id,
                    source_term,
                    suggested_translation,
                    occurrence_count,
                    status
                )
                values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString(term.id),
            UUID.fromString(term.bookId),
            term.chapterId?.let(UUID::fromString),
            term.sourceTerm,
            term.suggestedTranslation,
            term.occurrenceCount,
            term.status,
        )
        return term
    }

    override fun pendingTerms(bookId: String?): List<PendingGlossaryTerm> {
        val sql = if (bookId.isNullOrBlank()) {
            "$PENDING_TERMS_SQL where status = ? order by occurrence_count desc, source_term"
        } else {
            "$PENDING_TERMS_SQL where status = ? and book_id = ? order by occurrence_count desc, source_term"
        }
        val args = if (bookId.isNullOrBlank()) {
            arrayOf("PENDING")
        } else {
            arrayOf("PENDING", UUID.fromString(bookId))
        }
        return databaseClient.query(sql, *args).map(::mapPendingTerm)
    }

    override fun confirmPendingTerm(id: String, request: ConfirmPendingGlossaryTermRequest): GlossaryTerm {
        val pending = databaseClient.query(
            "$PENDING_TERMS_SQL where id = ? limit 1",
            UUID.fromString(id),
        ).firstOrNull()?.let(::mapPendingTerm) ?: throw NoSuchElementException("Pending glossary term not found: $id")
        databaseClient.update(
            """
                update pending_glossary_term
                set status = ?
                where id = ?
            """.trimIndent(),
            "CONFIRMED",
            UUID.fromString(id),
        )
        return addTerm(
            AddGlossaryTermRequest(
                bookId = pending.bookId,
                sourceTerm = pending.sourceTerm,
                translatedTerm = request.translatedTerm,
                type = request.type,
                description = request.description,
            ),
        )
    }

    private fun mapTerm(row: Map<String, Any?>): GlossaryTerm = GlossaryTerm(
        id = row.stringValue("id"),
        bookId = row.stringValue("book_id"),
        sourceTerm = row.stringValue("source_term"),
        translatedTerm = row.stringValue("translated_term"),
        type = GlossaryTermType.valueOf(row.stringValue("term_type")),
        description = row.stringValue("description"),
        enabled = row.booleanValue("enabled"),
    )

    private fun mapPendingTerm(row: Map<String, Any?>): PendingGlossaryTerm = PendingGlossaryTerm(
        id = row.stringValue("id"),
        bookId = row.stringValue("book_id"),
        chapterId = row.stringValue("chapter_id").ifBlank { null },
        sourceTerm = row.stringValue("source_term"),
        suggestedTranslation = row.stringValue("suggested_translation").ifBlank { null },
        occurrenceCount = row.intValue("occurrence_count"),
        status = row.stringValue("status"),
    )

    companion object {
        private const val TERMS_SQL = """
            select
                id::text as id,
                book_id::text as book_id,
                source_term,
                translated_term,
                term_type,
                description,
                enabled
            from glossary_term
        """
        private const val PENDING_TERMS_SQL = """
            select
                id::text as id,
                book_id::text as book_id,
                chapter_id::text as chapter_id,
                source_term,
                suggested_translation,
                occurrence_count,
                status
            from pending_glossary_term
        """
    }
}

@Configuration
class GlossaryConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun glossaryService(): GlossaryService =
        when (environment.getProperty("app.glossary.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcGlossaryService(databaseClient())
            else -> InMemoryGlossaryService()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
