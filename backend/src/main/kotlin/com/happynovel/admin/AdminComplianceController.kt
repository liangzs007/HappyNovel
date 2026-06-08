package com.happynovel.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
class ComplianceConfiguration {
    @Bean
    fun compliancePolicyService(): CompliancePolicyService = InMemoryCompliancePolicyService()
}
