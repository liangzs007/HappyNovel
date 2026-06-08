package com.happynovel.audit

import com.happynovel.admin.stringValue
import com.happynovel.content.ContentDatabaseClient
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

data class AuditLogEntry(
    val id: String,
    val actor: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val summary: String,
    val createdAt: Instant,
)

interface AuditLogService {
    fun record(
        actor: String,
        action: String,
        targetType: String,
        targetId: String,
        summary: String,
    ): AuditLogEntry
    fun entries(): List<AuditLogEntry>
}

class InMemoryAuditLogService : AuditLogService {
    private val mutableEntries = mutableListOf<AuditLogEntry>()

    override fun record(
        actor: String,
        action: String,
        targetType: String,
        targetId: String,
        summary: String,
    ): AuditLogEntry {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            actor = actor,
            action = action,
            targetType = targetType,
            targetId = targetId,
            summary = summary,
            createdAt = Instant.now(),
        )
        mutableEntries += entry
        return entry
    }

    override fun entries(): List<AuditLogEntry> = mutableEntries.toList()
}

class JdbcAuditLogService(
    private val databaseClient: ContentDatabaseClient,
) : AuditLogService {
    override fun record(
        actor: String,
        action: String,
        targetType: String,
        targetId: String,
        summary: String,
    ): AuditLogEntry {
        val entry = AuditLogEntry(
            id = UUID.randomUUID().toString(),
            actor = actor,
            action = action,
            targetType = targetType,
            targetId = targetId,
            summary = summary,
            createdAt = Instant.now(),
        )
        databaseClient.update(
            """
                insert into audit_log(
                    id,
                    actor,
                    action,
                    target_type,
                    target_id,
                    summary,
                    created_at
                )
                values (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString(entry.id),
            entry.actor,
            entry.action,
            entry.targetType,
            entry.targetId,
            entry.summary,
            entry.createdAt,
        )
        return entry
    }

    override fun entries(): List<AuditLogEntry> =
        databaseClient.query(
            """
                select
                    id::text as id,
                    actor,
                    action,
                    target_type,
                    target_id,
                    summary,
                    created_at
                from audit_log
                order by created_at desc
                limit 200
            """.trimIndent(),
        ).map(::mapEntry)

    private fun mapEntry(row: Map<String, Any?>): AuditLogEntry = AuditLogEntry(
        id = row.stringValue("id"),
        actor = row.stringValue("actor"),
        action = row.stringValue("action"),
        targetType = row.stringValue("target_type"),
        targetId = row.stringValue("target_id"),
        summary = row.stringValue("summary"),
        createdAt = when (val value = row["created_at"]) {
            is Instant -> value
            is OffsetDateTime -> value.toInstant()
            else -> Instant.parse(value.toString())
        },
    )
}
