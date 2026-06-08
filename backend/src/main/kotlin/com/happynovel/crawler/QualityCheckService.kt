package com.happynovel.crawler

class QualityCheckService {
    fun evaluate(title: String, paragraphs: List<String>, adBlocklist: List<String>): QualityCheckResult {
        val reasons = mutableListOf<String>()
        val joined = paragraphs.joinToString("\n")

        if (joined.length < 8) {
            reasons += "正文过短"
        }
        if (paragraphs.any { paragraph -> adBlocklist.any { keyword -> paragraph.contains(keyword) } }) {
            reasons += "疑似广告残留"
        }
        if (joined.count { it == '�' } > 3) {
            reasons += "疑似乱码"
        }

        val status = when {
            reasons.contains("正文过短") -> CleanQualityStatus.BLOCKED
            reasons.isNotEmpty() -> CleanQualityStatus.NEEDS_REVIEW
            title.isBlank() -> CleanQualityStatus.NEEDS_REVIEW
            else -> CleanQualityStatus.PASSED
        }

        return QualityCheckResult(status = status, reasons = reasons)
    }
}
