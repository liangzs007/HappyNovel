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
}

private const val BOOK_ID = "00000000-0000-0000-0000-000000000101"

private class RecordingGlossaryDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val terms = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        val onlyEnabled = sql.contains("enabled = true")
        val bookId = args.firstOrNull()?.toString()
        return terms
            .filter { bookId == null || it["book_id"] == bookId }
            .filter { !onlyEnabled || it["enabled"] == true }
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
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
}
