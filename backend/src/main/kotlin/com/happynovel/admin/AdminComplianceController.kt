package com.happynovel.admin

import com.happynovel.content.ContentDatabaseClient
import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class ComplianceConfigResponse(
    val privacyPolicyTitle: String,
    val privacyPolicyUrl: String,
    val termsTitle: String,
    val termsUrl: String,
    val adDisclosureEnabled: Boolean,
    val adDisclosureText: String,
)

data class CopyrightComplaintRow(
    val id: String = UUID.randomUUID().toString(),
    val source: String,
    val bookTitle: String,
    val chapterTitle: String,
    val status: String,
    val note: String,
)

data class AdminComplianceResponse(
    val config: ComplianceConfigResponse,
    val adConfig: AdminAdConfigResponse,
    val complaints: List<CopyrightComplaintRow>,
    val emptyText: String,
)

data class UpdateComplianceConfigRequest(
    val privacyPolicyTitle: String,
    val privacyPolicyUrl: String,
    val termsTitle: String,
    val termsUrl: String,
    val adDisclosureEnabled: Boolean,
    val adDisclosureText: String,
)

data class CreateCopyrightComplaintRequest(
    val source: String,
    val bookTitle: String,
    val chapterTitle: String,
    val note: String,
)

interface CompliancePolicyService {
    fun current(): ComplianceConfigResponse

    fun update(request: UpdateComplianceConfigRequest): ComplianceConfigResponse

    fun complaints(): List<CopyrightComplaintRow>

    fun createComplaint(request: CreateCopyrightComplaintRequest): CopyrightComplaintRow
}

class InMemoryCompliancePolicyService : CompliancePolicyService {
    private var config = ComplianceConfigResponse(
        privacyPolicyTitle = "HappyNovel Privacy Policy",
        privacyPolicyUrl = "https://example.com/privacy",
        termsTitle = "HappyNovel Terms of Service",
        termsUrl = "https://example.com/terms",
        adDisclosureEnabled = true,
        adDisclosureText = "This app may show ads to support translated novel reading.",
    )
    private val complaints = mutableListOf<CopyrightComplaintRow>()

    override fun current(): ComplianceConfigResponse = config

    override fun update(request: UpdateComplianceConfigRequest): ComplianceConfigResponse {
        config = ComplianceConfigResponse(
            privacyPolicyTitle = request.privacyPolicyTitle,
            privacyPolicyUrl = request.privacyPolicyUrl,
            termsTitle = request.termsTitle,
            termsUrl = request.termsUrl,
            adDisclosureEnabled = request.adDisclosureEnabled,
            adDisclosureText = request.adDisclosureText,
        )
        return config
    }

    override fun complaints(): List<CopyrightComplaintRow> = complaints.toList()

    override fun createComplaint(request: CreateCopyrightComplaintRequest): CopyrightComplaintRow {
        val complaint = CopyrightComplaintRow(
            source = request.source,
            bookTitle = request.bookTitle,
            chapterTitle = request.chapterTitle,
            status = "待处理",
            note = request.note,
        )
        complaints += complaint
        return complaint
    }
}

class JdbcCompliancePolicyService(
    private val databaseClient: ContentDatabaseClient,
) : CompliancePolicyService {
    override fun current(): ComplianceConfigResponse =
        databaseClient.query(
            """
                select
                    privacy_policy_title,
                    privacy_policy_url,
                    terms_title,
                    terms_url,
                    ad_disclosure_enabled,
                    ad_disclosure
                from compliance_config
                where id = ?
                limit 1
            """.trimIndent(),
            GLOBAL_COMPLIANCE_CONFIG_ID,
        ).firstOrNull()?.let(::mapComplianceConfig) ?: DEFAULT_CONFIG

    override fun update(request: UpdateComplianceConfigRequest): ComplianceConfigResponse {
        val config = ComplianceConfigResponse(
            privacyPolicyTitle = request.privacyPolicyTitle,
            privacyPolicyUrl = request.privacyPolicyUrl,
            termsTitle = request.termsTitle,
            termsUrl = request.termsUrl,
            adDisclosureEnabled = request.adDisclosureEnabled,
            adDisclosureText = request.adDisclosureText,
        )
        databaseClient.update(
            """
                insert into compliance_config(
                    id,
                    privacy_policy_title,
                    privacy_policy_url,
                    terms_title,
                    terms_url,
                    ad_disclosure_enabled,
                    ad_disclosure
                )
                values (?, ?, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    privacy_policy_title = excluded.privacy_policy_title,
                    privacy_policy_url = excluded.privacy_policy_url,
                    terms_title = excluded.terms_title,
                    terms_url = excluded.terms_url,
                    ad_disclosure_enabled = excluded.ad_disclosure_enabled,
                    ad_disclosure = excluded.ad_disclosure,
                    updated_at = now()
            """.trimIndent(),
            GLOBAL_COMPLIANCE_CONFIG_ID,
            config.privacyPolicyTitle,
            config.privacyPolicyUrl,
            config.termsTitle,
            config.termsUrl,
            config.adDisclosureEnabled,
            config.adDisclosureText,
        )
        return config
    }

    override fun complaints(): List<CopyrightComplaintRow> =
        databaseClient.query(
            """
                select id::text as id, source, book_title, chapter_title, status, note
                from copyright_complaint
                order by created_at desc
            """.trimIndent(),
        ).map(::mapComplaint)

    override fun createComplaint(request: CreateCopyrightComplaintRequest): CopyrightComplaintRow {
        val complaint = CopyrightComplaintRow(
            source = request.source,
            bookTitle = request.bookTitle,
            chapterTitle = request.chapterTitle,
            status = "待处理",
            note = request.note,
        )
        databaseClient.update(
            """
                insert into copyright_complaint(
                    id,
                    source,
                    book_title,
                    chapter_title,
                    status,
                    note,
                    complainant,
                    description
                )
                values (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.fromString(complaint.id),
            complaint.source,
            complaint.bookTitle,
            complaint.chapterTitle,
            complaint.status,
            complaint.note,
            complaint.source,
            complaint.note,
        )
        return complaint
    }

    private fun mapComplianceConfig(row: Map<String, Any?>): ComplianceConfigResponse = ComplianceConfigResponse(
        privacyPolicyTitle = row.stringValue("privacy_policy_title").ifBlank { DEFAULT_CONFIG.privacyPolicyTitle },
        privacyPolicyUrl = row.stringValue("privacy_policy_url").ifBlank { DEFAULT_CONFIG.privacyPolicyUrl },
        termsTitle = row.stringValue("terms_title").ifBlank { DEFAULT_CONFIG.termsTitle },
        termsUrl = row.stringValue("terms_url").ifBlank { DEFAULT_CONFIG.termsUrl },
        adDisclosureEnabled = row.booleanValue("ad_disclosure_enabled"),
        adDisclosureText = row.stringValue("ad_disclosure").ifBlank { DEFAULT_CONFIG.adDisclosureText },
    )

    private fun mapComplaint(row: Map<String, Any?>): CopyrightComplaintRow = CopyrightComplaintRow(
        id = row.stringValue("id"),
        source = row.stringValue("source"),
        bookTitle = row.stringValue("book_title"),
        chapterTitle = row.stringValue("chapter_title"),
        status = row.stringValue("status"),
        note = row.stringValue("note"),
    )

    companion object {
        private val GLOBAL_COMPLIANCE_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000201")
        private val DEFAULT_CONFIG = ComplianceConfigResponse(
            privacyPolicyTitle = "HappyNovel Privacy Policy",
            privacyPolicyUrl = "https://example.com/privacy",
            termsTitle = "HappyNovel Terms of Service",
            termsUrl = "https://example.com/terms",
            adDisclosureEnabled = true,
            adDisclosureText = "This app may show ads to support translated novel reading.",
        )
    }
}

@RestController
@RequestMapping("/api/admin/compliance")
class AdminComplianceController(
    private val compliancePolicyService: CompliancePolicyService,
    private val adConfigService: AdConfigService,
) {
    @GetMapping
    fun compliance(): AdminComplianceResponse = AdminComplianceResponse(
        config = compliancePolicyService.current(),
        adConfig = adConfigService.current(),
        complaints = compliancePolicyService.complaints(),
        emptyText = "暂无版权投诉记录",
    )

    @PutMapping
    fun updateCompliance(@RequestBody request: UpdateComplianceConfigRequest): AdminComplianceResponse = AdminComplianceResponse(
        config = compliancePolicyService.update(request),
        adConfig = adConfigService.current(),
        complaints = compliancePolicyService.complaints(),
        emptyText = "暂无版权投诉记录",
    )

    @PutMapping("/ad-config")
    fun updateAdConfig(@RequestBody request: UpdateAdConfigRequest): AdminComplianceResponse = AdminComplianceResponse(
        config = compliancePolicyService.current(),
        adConfig = adConfigService.update(request),
        complaints = compliancePolicyService.complaints(),
        emptyText = "暂无版权投诉记录",
    )

    @PostMapping("/complaints")
    fun createComplaint(@RequestBody request: CreateCopyrightComplaintRequest): AdminComplianceResponse {
        compliancePolicyService.createComplaint(request)
        return AdminComplianceResponse(
            config = compliancePolicyService.current(),
            adConfig = adConfigService.current(),
            complaints = compliancePolicyService.complaints(),
            emptyText = "暂无版权投诉记录",
        )
    }
}

@Configuration
class ComplianceConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun compliancePolicyService(): CompliancePolicyService =
        when (environment.getProperty("app.admin.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcCompliancePolicyService(databaseClient())
            else -> InMemoryCompliancePolicyService()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
