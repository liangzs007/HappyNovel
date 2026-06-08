package com.happynovel.audit

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbcAuditLogServiceTest {
    @Test
    fun `jdbc audit log service records and reads entries`() {
        val databaseClient = RecordingAuditDatabaseClient()
        val service = JdbcAuditLogService(databaseClient)

        val recorded = service.record(
            actor = "admin",
            action = "ADMIN_LOGIN",
            targetType = "admin_user",
            targetId = "admin",
            summary = "管理员登录",
        )

        assertEquals(listOf(recorded), service.entries())
        assertTrue(databaseClient.updates.single().contains("insert into audit_log"))
    }
}

private class RecordingAuditDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private val entries = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> = when {
        sql.contains("from audit_log") -> entries.toList()
        else -> emptyList()
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        entries += mapOf(
            "id" to args[0].toString(),
            "actor" to args[1],
            "action" to args[2],
            "target_type" to args[3],
            "target_id" to args[4],
            "summary" to args[5],
            "created_at" to args[6],
        )
        return 1
    }
}
