package com.happynovel.reading

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbcReadingEventServiceTest {
    @Test
    fun `jdbc reading event service records normalized event and anonymous device`() {
        val databaseClient = RecordingReadingEventDatabaseClient()
        val service = JdbcReadingEventService(databaseClient)

        val recorded = service.record(
            ReadingEvent(
                deviceId = "anon-00000000-0000-0000-0000-000000000001",
                bookId = "00000000-0000-0000-0000-000000000101",
                chapterId = "00000000-0000-0000-0000-000000000201",
                percent = 1.25f,
            ),
        )

        assertEquals(1f, recorded.percent)
        assertEquals(listOf(recorded), service.events())
        assertTrue(databaseClient.updates[0].contains("insert into anonymous_device"))
        assertTrue(databaseClient.updates[1].contains("insert into reading_event"))
    }
}

private class RecordingReadingEventDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val events = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> = when {
        sql.contains("from reading_event") -> events.toList()
        else -> emptyList()
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        if (sql.contains("reading_event")) {
            events += mapOf(
                "device_id" to "anon-${args[1]}",
                "book_id" to args[2].toString(),
                "chapter_id" to args[3].toString(),
                "progress_percent" to args[5],
            )
        }
        return 1
    }
}
