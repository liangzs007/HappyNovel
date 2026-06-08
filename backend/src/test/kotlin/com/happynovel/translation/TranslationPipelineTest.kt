package com.happynovel.translation

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TranslationPipelineTest {
    @Test
    fun `glossary stores enabled book terms`() {
        val glossary = InMemoryGlossaryService()

        val term = glossary.addTerm(
            AddGlossaryTermRequest(
                bookId = "book-1",
                sourceTerm = "青云宗",
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "主角初入的宗门",
            )
        )

        assertEquals("Azure Cloud Sect", term.translatedTerm)
        assertEquals(listOf(term), glossary.enabledTerms("book-1"))
        assertEquals(listOf(term), glossary.terms("book-1"))
        assertEquals(listOf(term), glossary.terms())
    }

    @Test
    fun `chunker keeps paragraph boundaries`() {
        val chunker = ChapterChunker(maxCharacters = 24)

        val chunks = chunker.chunk(
            listOf(
                "第一段文字很短。",
                "第二段文字也很短。",
                "第三段文字需要进入下一块。",
            )
        )

        assertEquals(2, chunks.size)
        assertEquals(listOf("第一段文字很短。", "第二段文字也很短。"), chunks.first().paragraphs)
        assertEquals(listOf("第三段文字需要进入下一块。"), chunks.last().paragraphs)
    }

    @Test
    fun `prompt includes glossary and paragraph structure instruction`() {
        val prompt = TranslationPromptBuilder().build(
            title = "第一章 青云宗",
            paragraphs = listOf("少年林辰踏入青云宗。"),
            targetLanguage = "en",
            glossary = listOf(
                GlossaryTerm(
                    bookId = "book-1",
                    sourceTerm = "青云宗",
                    translatedTerm = "Azure Cloud Sect",
                    type = GlossaryTermType.ORGANIZATION,
                    description = "宗门",
                )
            )
        )

        assertTrue(prompt.contains("Translate the chapter into en"))
        assertTrue(prompt.contains("青云宗 => Azure Cloud Sect"))
        assertTrue(prompt.contains("Preserve paragraph count and order"))
    }

    @Test
    fun `translation task uses provider records cost and auto publishes`() {
        val glossary = InMemoryGlossaryService()
        glossary.addTerm(
            AddGlossaryTermRequest(
                bookId = "book-1",
                sourceTerm = "青云宗",
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "宗门",
            )
        )
        val provider = FakeTranslationProvider(
            translatedParagraphs = listOf("Lin Chen entered Azure Cloud Sect."),
            inputTokens = 120,
            outputTokens = 40,
        )
        val service = TranslationPipelineService(
            glossaryService = glossary,
            provider = provider,
            chunker = ChapterChunker(maxCharacters = 500),
            promptBuilder = TranslationPromptBuilder(),
        )

        val result = service.translateChapter(
            TranslateChapterRequest(
                bookId = "book-1",
                chapterId = "chapter-1",
                title = "第一章 青云宗",
                sourceLanguage = "zh",
                targetLanguage = "en",
                paragraphs = listOf("少年林辰踏入青云宗。"),
            )
        )

        assertEquals(TranslationTaskStatus.SUCCEEDED, result.status)
        assertEquals(PublicationStatus.PUBLISHED, result.publicationStatus)
        assertEquals("openai", result.provider)
        assertEquals("gpt-5-mini", result.model)
        assertEquals(120, result.inputTokens)
        assertEquals(40, result.outputTokens)
        assertTrue(result.estimatedCost > 0.toBigDecimal())
        assertEquals(listOf("Lin Chen entered Azure Cloud Sect."), result.paragraphs)
        assertTrue(provider.lastPrompt!!.contains("Azure Cloud Sect"))
    }

    @Test
    fun `translation task creates pending glossary candidates from source text`() {
        val glossary = InMemoryGlossaryService()
        glossary.addTerm(
            AddGlossaryTermRequest(
                bookId = "book-1",
                sourceTerm = "青云宗",
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "宗门",
            )
        )
        val service = TranslationPipelineService(
            glossaryService = glossary,
            provider = FakeTranslationProvider(
                translatedParagraphs = listOf("Lin Chen returned to Azure Cloud Sect."),
                inputTokens = 120,
                outputTokens = 40,
            ),
            chunker = ChapterChunker(maxCharacters = 500),
            promptBuilder = TranslationPromptBuilder(),
        )

        service.translateChapter(
            TranslateChapterRequest(
                bookId = "book-1",
                chapterId = "chapter-1",
                title = "第一章 青云宗",
                sourceLanguage = "zh",
                targetLanguage = "en",
                paragraphs = listOf("林辰拜入青云宗。林辰在青云宗遇见苏瑶。苏瑶提醒林辰。"),
            )
        )

        val pendingTerms = glossary.pendingTerms("book-1")

        assertEquals(listOf("林辰", "苏瑶"), pendingTerms.map { it.sourceTerm })
        assertEquals(listOf(3, 2), pendingTerms.map { it.occurrenceCount })
        assertTrue(pendingTerms.all { it.chapterId == "chapter-1" })
    }

    @Test
    fun `openai provider builds responses api request payload`() {
        val provider = OpenAITranslationProvider(
            apiKey = "test-key",
            model = "gpt-5-mini",
            httpClient = CapturingOpenAIHttpClient(
                response = OpenAIProviderRawResponse(
                    text = "{\"title\":\"Chapter 1\",\"paragraphs\":[\"Translated paragraph\"]}",
                    inputTokens = 10,
                    outputTokens = 5,
                )
            ),
        )

        val result = provider.translate(
            TranslationProviderRequest(
                title = "第一章",
                prompt = "Translate this chapter",
                targetLanguage = "en",
                paragraphCount = 1,
            )
        )

        assertEquals(listOf("Translated paragraph"), result.paragraphs)
        assertEquals(10, result.inputTokens)
        assertEquals(5, result.outputTokens)
        assertEquals("gpt-5-mini", result.model)
    }
}
