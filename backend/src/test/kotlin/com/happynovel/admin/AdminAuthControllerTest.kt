package com.happynovel.admin

import com.happynovel.audit.InMemoryAuditLogService
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AdminAuthControllerTest {
    @Test
    fun `login returns admin session for configured credentials`() {
        val auditLogService = InMemoryAuditLogService()
        val controller = AdminAuthController(
            authService = AdminAuthService(
                username = "admin",
                password = "secret",
                auditLogService = auditLogService,
            )
        )

        val response = controller.login(AdminLoginRequest("admin", "secret"))

        assertEquals("admin", response.username)
        assertTrue(response.token.startsWith("admin-session-"))
        assertEquals("ADMIN_LOGIN", auditLogService.entries().single().action)
    }

    @Test
    fun `login rejects invalid credentials`() {
        val controller = AdminAuthController(
            authService = AdminAuthService(
                username = "admin",
                password = "secret",
                auditLogService = InMemoryAuditLogService(),
            )
        )

        assertFailsWith<InvalidAdminCredentialsException> {
            controller.login(AdminLoginRequest("admin", "wrong"))
        }
    }
}
