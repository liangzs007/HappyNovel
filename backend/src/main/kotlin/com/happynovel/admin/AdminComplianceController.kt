package com.happynovel.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class ComplianceConfigResponse(
    val privacyPolicyTitle: String,
    val privacyPolicyUrl: String,
    val termsTitle: String,
    val termsUrl: String,
    val adDisclosureEnabled: Boolean,
    val adDisclosureText: String,
)

data class CopyrightComplaintRow(
    val id: String,
    val source: String,
    val bookTitle: String,
    val chapterTitle: String,
    val status: String,
    val note: String,
)

data class AdminComplianceResponse(
    val config: ComplianceConfigResponse,
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

interface CompliancePolicyService {
    fun current(): ComplianceConfigResponse

    fun update(request: UpdateComplianceConfigRequest): ComplianceConfigResponse
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
}

@RestController
@RequestMapping("/api/admin/compliance")
class AdminComplianceController(
    private val compliancePolicyService: CompliancePolicyService,
) {
    @GetMapping
    fun compliance(): AdminComplianceResponse = AdminComplianceResponse(
        config = compliancePolicyService.current(),
        complaints = emptyList(),
        emptyText = "暂无版权投诉记录",
    )

    @PutMapping
    fun updateCompliance(@RequestBody request: UpdateComplianceConfigRequest): AdminComplianceResponse = AdminComplianceResponse(
        config = compliancePolicyService.update(request),
        complaints = emptyList(),
        emptyText = "暂无版权投诉记录",
    )
}

@Configuration
class ComplianceConfiguration {
    @Bean
    fun compliancePolicyService(): CompliancePolicyService = InMemoryCompliancePolicyService()
}
