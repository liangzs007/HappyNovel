package com.happynovel.audit

import java.time.Instant
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
}

class InMemoryAuditLogService : AuditLogService {
    private val mutableEntries = mutableListOf<AuditLogEntry>()

    val entries: List<AuditLogEntry>
        get() = mutableEntries.toList()

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
}
