package com.happynovel.crawler

class NovelHtmlParser {
    private val linkRegex = Regex("<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", RegexOption.IGNORE_CASE)
    private val titleRegex = Regex("<h1[^>]*>(.*?)</h1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    private val contentRegex = Regex("<div[^>]*class=[\"'][^\"']*chapter-content[^\"']*[\"'][^>]*>(.*?)</div>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

    fun parseChapterLinks(html: String): List<ChapterLink> = linkRegex.findAll(html)
        .map { match ->
            ChapterLink(
                title = stripTags(match.groupValues[2]).trim(),
                url = match.groupValues[1].trim(),
            )
        }
        .filter { it.title.isNotBlank() && it.url.isNotBlank() }
        .toList()

    fun parseChapterBody(html: String): ParsedChapterBody {
        val title = titleRegex.find(html)?.groupValues?.get(1)?.let(::stripTags)?.trim().orEmpty()
        val rawBody = contentRegex.find(html)?.groupValues?.get(1)?.trim().orEmpty()
        return ParsedChapterBody(title = title, rawBody = rawBody)
    }

    private fun stripTags(value: String): String = value.replace(Regex("<[^>]+>"), "")
}
