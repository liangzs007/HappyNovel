package com.happynovel.admin

import com.happynovel.audit.InMemoryAuditLogService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminAuditControllerTest {
    @Test
    fun `admin audit endpoint returns recorded entries`() {
        val auditLogService = InMemoryAuditLogService()
        auditLogService.record(
            actor = "admin",
            action = "BOOK_TAKEDOWN",
            targetType = "book",
            targetId = "book-seed-1",
            summary = "下架测试书籍",
        )
        val controller = AdminAuditController(auditLogService)

        val response = controller.auditLogs()

        assertEquals("暂无审计记录。", response.emptyText)
        assertEquals(1, response.entries.size)
        assertEquals("admin", response.entries.single().actor)
        assertEquals("BOOK_TAKEDOWN", response.entries.single().action)
        assertEquals("book:book-seed-1", response.entries.single().target)
        assertEquals("下架测试书籍", response.entries.single().summary)
    }
}
