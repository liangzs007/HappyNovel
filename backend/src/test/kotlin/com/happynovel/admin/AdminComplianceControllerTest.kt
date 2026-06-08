package com.happynovel.admin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminComplianceControllerTest {
    @Test
    fun `admin compliance endpoint returns policy config and complaint rows`() {
        val controller = AdminComplianceController(InMemoryCompliancePolicyService(), InMemoryAdConfigService())

        val response = controller.compliance()

        assertEquals("HappyNovel Privacy Policy", response.config.privacyPolicyTitle)
        assertEquals("HappyNovel Terms of Service", response.config.termsTitle)
        assertEquals(true, response.config.adDisclosureEnabled)
        assertEquals("暂无版权投诉记录", response.emptyText)
        assertEquals(emptyList<CopyrightComplaintRow>(), response.complaints)
    }

    @Test
    fun `admin can update compliance policy config`() {
        val controller = AdminComplianceController(InMemoryCompliancePolicyService(), InMemoryAdConfigService())

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

    @Test
    fun `admin can create copyright complaint record`() {
        val controller = AdminComplianceController(InMemoryCompliancePolicyService(), InMemoryAdConfigService())

        val response = controller.createComplaint(
            CreateCopyrightComplaintRequest(
                source = "email",
                bookTitle = "测试书籍",
                chapterTitle = "第一章",
                note = "权利人要求下架章节",
            ),
        )

        val complaint = response.complaints.single()
        assertEquals("email", complaint.source)
        assertEquals("测试书籍", complaint.bookTitle)
        assertEquals("第一章", complaint.chapterTitle)
        assertEquals("待处理", complaint.status)
        assertEquals("权利人要求下架章节", complaint.note)
    }

    @Test
    fun `admin can update app ad config`() {
        val adConfigService = InMemoryAdConfigService()
        val controller = AdminComplianceController(InMemoryCompliancePolicyService(), adConfigService)

        val response = controller.updateAdConfig(
            UpdateAdConfigRequest(
                enabled = false,
                readerBannerEnabled = false,
                interstitialEveryChapters = 8,
            ),
        )

        assertEquals(false, response.adConfig.enabled)
        assertEquals(false, response.adConfig.readerBannerEnabled)
        assertEquals(8, response.adConfig.interstitialEveryChapters)
        assertEquals(response.adConfig, adConfigService.current())
    }
}
