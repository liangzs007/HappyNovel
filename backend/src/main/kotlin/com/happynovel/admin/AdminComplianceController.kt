package com.happynovel.admin

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

@RestController
@RequestMapping("/api/admin/compliance")
class AdminComplianceController {
    @GetMapping
    fun compliance(): AdminComplianceResponse = AdminComplianceResponse(
        config = ComplianceConfigResponse(
            privacyPolicyTitle = "HappyNovel Privacy Policy",
            privacyPolicyUrl = "https://example.com/privacy",
            termsTitle = "HappyNovel Terms of Service",
            termsUrl = "https://example.com/terms",
            adDisclosureEnabled = true,
            adDisclosureText = "This app may show ads to support translated novel reading.",
        ),
        complaints = emptyList(),
        emptyText = "暂无版权投诉记录",
    )
}
