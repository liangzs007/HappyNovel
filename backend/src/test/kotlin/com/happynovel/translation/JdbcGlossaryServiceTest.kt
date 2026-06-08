package com.happynovel.translation

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbcGlossaryServiceTest {
    @Test
    fun `jdbc glossary service adds and lists terms by book`() {
        val databaseClient = RecordingGlossaryDatabaseClient()
        val service = JdbcGlossaryService(databaseClient)

        val term = service.addTerm(
            AddGlossaryTermRequest(
                bookId = BOOK_ID,
                sourceTerm = "青云宗",
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "主角初入的宗门",
            ),
        )

        assertEquals(listOf(term), service.terms(BOOK_ID))
        assertEquals(listOf(term), service.enabledTerms(BOOK_ID))
        assertEquals(emptyList(), service.terms("00000000-0000-0000-0000-000000000202"))
        assertTrue(databaseClient.updates.single().contains("insert into glossary_term"))
    }

    @Test
    fun `jdbc glossary service confirms pending terms into glossary`() {
        val databaseClient = RecordingGlossaryDatabaseClient()
        val service = JdbcGlossaryService(databaseClient)

        val pending = service.createPendingTerm(
            CreatePendingGlossaryTermRequest(
                bookId = BOOK_ID,
                chapterId = CHAPTER_ID,
                sourceTerm = "青云宗",
                suggestedTranslation = "Azure Cloud Sect",
                occurrenceCount = 3,
            ),
        )

        assertEquals(listOf(pending), service.pendingTerms(BOOK_ID))

        val confirmed = service.confirmPendingTerm(
            pending.id,
            ConfirmPendingGlossaryTermRequest(
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "主角初入的宗门",
            ),
        )

        assertEquals(emptyList(), service.pendingTerms(BOOK_ID))
        assertEquals(listOf(confirmed), service.terms(BOOK_ID))
        assertTrue(databaseClient.updates.any { it.contains("insert into pending_glossary_term") })
        assertTrue(databaseClient.updates.any { it.contains("update pending_glossary_term") })
    }
}

private const val BOOK_ID = "00000000-0000-0000-0000-000000000101"
private const val CHAPTER_ID = "00000000-0000-0000-0000-000000000201"

private class RecordingGlossaryDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val terms = mutableListOf<Map<String, Any?>>()
    private val pendingTerms = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        if (sql.contains("from pending_glossary_term")) {
            return queryPendingTerms(sql, args)
        }
        val onlyEnabled = sql.contains("enabled = true")
        val bookId = args.firstOrNull()?.toString()
        return terms
            .filter { bookId == null || it["book_id"] == bookId }
            .filter { !onlyEnabled || it["enabled"] == true }
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        if (sql.contains("insert into pending_glossary_term")) {
            pendingTerms += mapOf(
                "id" to args[0].toString(),
                "book_id" to args[1].toString(),
                "chapter_id" to args[2]?.toString(),
                "source_term" to args[3],
                "suggested_translation" to args[4],
                "occurrence_count" to args[5],
                "status" to args[6],
            )
            return 1
        }
        if (sql.contains("update pending_glossary_term")) {
            val status = args[0]
            val id = args[1].toString()
            pendingTerms.replaceAll {
                if (it["id"] == id) it + ("status" to status) else it
            }
            return 1
        }
        terms += mapOf(
            "id" to args[0].toString(),
            "book_id" to args[1].toString(),
            "source_term" to args[2],
            "translated_term" to args[3],
            "term_type" to args[4],
            "description" to args[5],
            "enabled" to args[6],
        )
        return 1
    }

    private fun queryPendingTerms(sql: String, args: Array<out Any?>): List<Map<String, Any?>> {
        if (sql.contains("where id = ?")) {
            val id = args[0].toString()
            return pendingTerms.filter { it["id"] == id }
        }

        val status = args[0]?.toString()
        val bookId = args.getOrNull(1)?.toString()
        return pendingTerms
            .filter { status == null || it["status"] == status }
            .filter { bookId == null || it["book_id"] == bookId }
    }
}
