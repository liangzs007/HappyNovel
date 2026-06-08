package com.happynovel.translation

import com.happynovel.admin.booleanValue
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

    private fun mapTerm(row: Map<String, Any?>): GlossaryTerm = GlossaryTerm(
        id = row.stringValue("id"),
        bookId = row.stringValue("book_id"),
        sourceTerm = row.stringValue("source_term"),
        translatedTerm = row.stringValue("translated_term"),
        type = GlossaryTermType.valueOf(row.stringValue("term_type")),
        description = row.stringValue("description"),
        enabled = row.booleanValue("enabled"),
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
