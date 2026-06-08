package com.happynovel.publication

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JdbcPublicationControlServiceTest {
    @Test
    fun `jdbc publication control reads and updates book and chapter status`() {
        val databaseClient = RecordingPublicationDatabaseClient()
        val service = JdbcPublicationControlService(databaseClient)

        assertTrue(service.isBookPublished(BOOK_ID))
        assertTrue(service.isChapterPublished(CHAPTER_ID))

        service.unpublishBook(BOOK_ID)
        service.hideChapter(CHAPTER_ID)

        assertFalse(service.isBookPublished(BOOK_ID))
        assertFalse(service.isChapterPublished(CHAPTER_ID))
        assertTrue(databaseClient.updates.any { it.contains("update book") })
        assertTrue(databaseClient.updates.any { it.contains("update chapter") })
    }
}

private const val BOOK_ID = "00000000-0000-0000-0000-000000000101"
private const val CHAPTER_ID = "00000000-0000-0000-0000-000000000201"

private class RecordingPublicationDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val bookStatus = mutableMapOf(BOOK_ID to "published")
    private val chapterStatus = mutableMapOf(CHAPTER_ID to "published")

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> = when {
        sql.contains("from book") -> listOf(mapOf("publication_status" to bookStatus[args[0].toString()]))
        sql.contains("from chapter") -> listOf(mapOf("publication_status" to chapterStatus[args[0].toString()]))
        else -> emptyList()
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        when {
            sql.contains("update book") -> bookStatus[args[1].toString()] = args[0].toString()
            sql.contains("update chapter") -> chapterStatus[args[1].toString()] = args[0].toString()
        }
        return 1
    }
}
