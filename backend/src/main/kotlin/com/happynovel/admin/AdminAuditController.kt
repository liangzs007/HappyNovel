package com.happynovel.admin

import com.happynovel.audit.AuditLogEntry
import com.happynovel.audit.AuditLogService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class AdminAuditLogRow(
    val id: String,
    val actor: String,
    val action: String,
    val target: String,
    val summary: String,
    val createdAt: String,
)

data class AdminAuditLogsResponse(
    val entries: List<AdminAuditLogRow>,
    val emptyText: String,
)

@RestController
@RequestMapping("/api/admin/audit")
class AdminAuditController(
    private val auditLogService: AuditLogService,
) {
    @GetMapping
    fun auditLogs(): AdminAuditLogsResponse = AdminAuditLogsResponse(
        entries = auditLogService.entries()
            .sortedByDescending { it.createdAt }
            .map(::toAdminAuditLogRow),
        emptyText = "暂无审计记录。",
    )
}

private fun toAdminAuditLogRow(entry: AuditLogEntry): AdminAuditLogRow = AdminAuditLogRow(
    id = entry.id,
    actor = entry.actor,
    action = entry.action,
    target = "${entry.targetType}:${entry.targetId}",
    summary = entry.summary,
    createdAt = entry.createdAt.toString(),
)
