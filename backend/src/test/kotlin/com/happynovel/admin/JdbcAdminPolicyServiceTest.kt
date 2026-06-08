package com.happynovel.admin

import com.happynovel.content.ContentDatabaseClient
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JdbcAdminPolicyServiceTest {
    @Test
    fun `jdbc ad config service persists updates through database client`() {
        val databaseClient = RecordingAdminPolicyDatabaseClient()
        val service = JdbcAdConfigService(databaseClient)

        assertEquals(AdminAdConfigResponse(true, true, 5), service.current())

        val updated = service.update(
            UpdateAdConfigRequest(
                enabled = false,
                readerBannerEnabled = false,
                interstitialEveryChapters = 8,
            ),
        )

        assertEquals(AdminAdConfigResponse(false, false, 8), updated)
        assertEquals(updated, service.current())
        assertTrue(databaseClient.updates.any { it.contains("insert into ad_config") })
    }

    @Test
    fun `jdbc compliance policy service persists policy and complaint rows`() {
        val databaseClient = RecordingAdminPolicyDatabaseClient()
        val service = JdbcCompliancePolicyService(databaseClient)

        val updated = service.update(
            UpdateComplianceConfigRequest(
                privacyPolicyTitle = "隐私政策",
                privacyPolicyUrl = "https://example.com/privacy-cn",
                termsTitle = "用户协议",
                termsUrl = "https://example.com/terms-cn",
                adDisclosureEnabled = false,
                adDisclosureText = "当前不展示广告。",
            ),
        )
        val complaint = service.createComplaint(
            CreateCopyrightComplaintRequest(
                source = "email",
                bookTitle = "Dragon Gate",
                chapterTitle = "Chapter 1",
                note = "版权方要求复核。",
            ),
        )

        assertEquals(updated, service.current())
        assertEquals(listOf(complaint), service.complaints())
        assertTrue(databaseClient.updates.any { it.contains("insert into compliance_config") })
        assertTrue(databaseClient.updates.any { it.contains("insert into copyright_complaint") })
    }
}

private class RecordingAdminPolicyDatabaseClient : ContentDatabaseClient {
    val updates = mutableListOf<String>()
    private var adConfig: Map<String, Any?>? = null
    private var complianceConfig: Map<String, Any?>? = null
    private val complaints = mutableListOf<Map<String, Any?>>()

    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> = when {
        sql.contains("from ad_config") -> listOfNotNull(adConfig)
        sql.contains("from compliance_config") -> listOfNotNull(complianceConfig)
        sql.contains("from copyright_complaint") -> complaints.toList()
        else -> emptyList()
    }

    override fun update(sql: String, vararg args: Any?): Int {
        updates += sql
        when {
            sql.contains("ad_config") -> adConfig = mapOf(
                "enabled" to args[3],
                "reader_banner_enabled" to args[4],
                "interstitial_every_chapters" to args[5],
            )

            sql.contains("compliance_config") -> complianceConfig = mapOf(
                "privacy_policy_title" to args[1],
                "privacy_policy_url" to args[2],
                "terms_title" to args[3],
                "terms_url" to args[4],
                "ad_disclosure_enabled" to args[5],
                "ad_disclosure" to args[6],
            )

            sql.contains("copyright_complaint") -> complaints += mapOf(
                "id" to args[0].toString(),
                "source" to args[1],
                "book_title" to args[2],
                "chapter_title" to args[3],
                "status" to args[4],
                "note" to args[5],
            )
        }
        return 1
    }
}
