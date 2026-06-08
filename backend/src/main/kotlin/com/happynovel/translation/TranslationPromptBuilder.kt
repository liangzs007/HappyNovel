package com.happynovel.translation

class TranslationPromptBuilder {
    fun build(
        title: String,
        paragraphs: List<String>,
        targetLanguage: String,
        glossary: List<GlossaryTerm>,
    ): String {
        val glossaryText = if (glossary.isEmpty()) {
            "No glossary terms."
        } else {
            glossary.joinToString("\n") { "${it.sourceTerm} => ${it.translatedTerm} (${it.type})" }
        }

        return buildString {
            appendLine("Translate the chapter into $targetLanguage.")
            appendLine("Preserve paragraph count and order.")
            appendLine("Return JSON with fields: title, paragraphs.")
            appendLine("Use these glossary terms consistently:")
            appendLine(glossaryText)
            appendLine("Title: $title")
            appendLine("Paragraphs:")
            paragraphs.forEachIndexed { index, paragraph -> appendLine("${index + 1}. $paragraph") }
        }
    }
}
