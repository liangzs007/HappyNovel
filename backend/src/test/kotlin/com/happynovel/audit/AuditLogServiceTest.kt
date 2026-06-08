package com.happynovel.audit

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditLogServiceTest {
    @Test
    fun `records auditable admin action`() {
        val service = InMemoryAuditLogService()

        val entry = service.record(
            actor = "admin",
            action = "BOOK_TAKEDOWN",
            targetType = "book",
            targetId = "book-seed-1",
            summary = "下架测试书籍",
        )

        assertEquals("BOOK_TAKEDOWN", entry.action)
        assertEquals("book-seed-1", entry.targetId)
        assertTrue(service.entries.contains(entry))
    }
}
