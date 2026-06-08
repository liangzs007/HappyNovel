package com.happynovel.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminComplianceControllerTest {
    @Test
    fun `admin compliance endpoint returns policy config and complaint rows`() {
        val controller = AdminComplianceController(InMemoryCompliancePolicyService())

        val response = controller.compliance()

        assertEquals("HappyNovel Privacy Policy", response.config.privacyPolicyTitle)
        assertEquals("HappyNovel Terms of Service", response.config.termsTitle)
        assertEquals(true, response.config.adDisclosureEnabled)
        assertEquals("暂无版权投诉记录", response.emptyText)
        assertEquals(emptyList<CopyrightComplaintRow>(), response.complaints)
    }

    @Test
    fun `admin can update compliance policy config`() {
        val controller = AdminComplianceController(InMemoryCompliancePolicyService())

        val response = controller.updateCompliance(
            UpdateComplianceConfigRequest(
                privacyPolicyTitle = "Updated Privacy",
                privacyPolicyUrl = "https://example.com/updated-privacy",
                termsTitle = "Updated Terms",
                termsUrl = "https://example.com/updated-terms",
                adDisclosureEnabled = false,
                adDisclosureText = "Ads are disabled for this region.",
            ),
        )

        assertEquals("Updated Privacy", response.config.privacyPolicyTitle)
        assertEquals("Updated Terms", response.config.termsTitle)
        assertEquals(false, response.config.adDisclosureEnabled)
        assertEquals("Ads are disabled for this region.", response.config.adDisclosureText)
    }
}
