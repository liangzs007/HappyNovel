package com.happynovel.reading

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

data class ReadingEvent(
    val deviceId: String,
    val bookId: String,
    val chapterId: String,
    val percent: Float,
)

interface ReadingEventService {
    fun record(event: ReadingEvent): ReadingEvent

    fun events(): List<ReadingEvent>
}

class InMemoryReadingEventService : ReadingEventService {
    private val events = mutableListOf<ReadingEvent>()

    override fun record(event: ReadingEvent): ReadingEvent {
        val normalized = event.copy(percent = event.percent.coerceIn(0f, 1f))
        events += normalized
        return normalized
    }

    override fun events(): List<ReadingEvent> = events.toList()
}

class JdbcReadingEventService(
    private val databaseClient: ContentDatabaseClient,
) : ReadingEventService {
    override fun record(event: ReadingEvent): ReadingEvent {
        val normalized = event.copy(percent = event.percent.coerceIn(0f, 1f))
        val deviceUuid = normalized.deviceId.removePrefix("anon-").let(UUID::fromString)
        databaseClient.update(
            """
                insert into anonymous_device(id, device_key)
                values (?, ?)
                on conflict (id) do update set device_key = excluded.device_key
            """.trimIndent(),
            deviceUuid,
            normalized.deviceId,
        )
        databaseClient.update(
            """
                insert into reading_event(
                    id,
                    device_id,
                    book_id,
                    chapter_id,
                    event_type,
                    progress_percent
                )
                values (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.randomUUID(),
            deviceUuid,
            UUID.fromString(normalized.bookId),
            UUID.fromString(normalized.chapterId),
            "progress",
            normalized.percent,
        )
        return normalized
    }

    override fun events(): List<ReadingEvent> =
        databaseClient.query(
            """
                select
                    concat('anon-', device_id::text) as device_id,
                    book_id::text as book_id,
                    chapter_id::text as chapter_id,
                    progress_percent
                from reading_event
                where event_type = ?
                order by created_at
            """.trimIndent(),
            "progress",
        ).map(::mapReadingEvent)

    private fun mapReadingEvent(row: Map<String, Any?>): ReadingEvent = ReadingEvent(
        deviceId = row.stringValue("device_id"),
        bookId = row.stringValue("book_id"),
        chapterId = row.stringValue("chapter_id"),
        percent = when (val value = row["progress_percent"]) {
            is Number -> value.toFloat()
            is String -> value.toFloat()
            else -> 0f
        },
    )
}

@Configuration
class ReadingEventConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun readingEventService(): ReadingEventService =
        when (environment.getProperty("app.reading.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcReadingEventService(databaseClient())
            else -> InMemoryReadingEventService()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
