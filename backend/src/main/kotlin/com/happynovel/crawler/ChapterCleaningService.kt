package com.happynovel.crawler

class ChapterCleaningService(
    private val qualityCheckService: QualityCheckService,
) {
    fun clean(raw: RawChapterContent, adBlocklist: List<String>): CleanChapterContent {
        val title = cleanTitle(raw.title, adBlocklist)
        val paragraphs = extractParagraphs(raw.rawContent)
            .map { cleanText(it, adBlocklist) }
            .filter { it.isNotBlank() }
        val quality = qualityCheckService.evaluate(title, paragraphs, adBlocklist)

        return CleanChapterContent(
            rawChapterId = raw.id,
            title = title,
            paragraphs = paragraphs,
            qualityStatus = quality.status,
            qualityReasons = quality.reasons,
        )
    }

    private fun cleanTitle(value: String, adBlocklist: List<String>): String = adBlocklist
        .fold(value) { title, keyword -> title.replace(keyword, "") }
        .replace(Regex("[_-]+$"), "")
        .trim()

    private fun extractParagraphs(html: String): List<String> {
        val content = html
            .replace(Regex("<script[^>]*>.*?</script>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")
            .replace(Regex("<div[^>]*class=[\"'][^\"']*nav[^\"']*[\"'][^>]*>.*?</div>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)), "")

        return Regex("<p[^>]*>(.*?)</p>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
            .findAll(content)
            .map { stripTags(it.groupValues[1]).trim() }
            .toList()
    }

    private fun cleanText(value: String, adBlocklist: List<String>): String {
        val withoutAds = adBlocklist.fold(value) { text, keyword -> text.replace(keyword, "") }
        return withoutAds.replace(Regex("\\s+"), " ").trim()
    }

    private fun stripTags(value: String): String = value.replace(Regex("<[^>]+>"), "")
}
