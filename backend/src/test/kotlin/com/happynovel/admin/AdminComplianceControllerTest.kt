package com.happynovel.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminComplianceControllerTest {
    @Test
    fun `admin compliance endpoint returns policy config and complaint rows`() {
        val controller = AdminComplianceController()

        val response = controller.compliance()

        assertEquals("HappyNovel Privacy Policy", response.config.privacyPolicyTitle)
        assertEquals("HappyNovel Terms of Service", response.config.termsTitle)
        assertEquals(true, response.config.adDisclosureEnabled)
        assertEquals("暂无版权投诉记录", response.emptyText)
        assertEquals(emptyList<CopyrightComplaintRow>(), response.complaints)
    }
}
