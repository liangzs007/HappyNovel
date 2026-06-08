package com.happynovel.translation

import java.math.BigDecimal

data class TranslateChapterRequest(
    val bookId: String,
    val chapterId: String,
    val title: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val paragraphs: List<String>,
)

enum class TranslationTaskStatus {
    CREATED,
    RUNNING,
    SUCCEEDED,
    FAILED,
}

enum class PublicationStatus {
    DRAFT,
    PUBLISHED,
    HIDDEN,
}

data class TranslationTaskResult(
    val chapterId: String,
    val language: String,
    val title: String,
    val paragraphs: List<String>,
    val provider: String,
    val model: String,
    val inputTokens: Int,
    val outputTokens: Int,
    val estimatedCost: BigDecimal,
    val status: TranslationTaskStatus,
    val publicationStatus: PublicationStatus,
)

class TranslationPipelineService(
    private val glossaryService: GlossaryService,
    private val provider: TranslationProvider,
    private val chunker: ChapterChunker,
    private val promptBuilder: TranslationPromptBuilder,
) {
    fun translateChapter(request: TranslateChapterRequest): TranslationTaskResult {
        val glossary = glossaryService.enabledTerms(request.bookId)
        val chunks = chunker.chunk(request.paragraphs)
        val translatedParagraphs = mutableListOf<String>()
        var translatedTitle = request.title
        var providerName = ""
        var model = ""
        var inputTokens = 0
        var outputTokens = 0

        createPendingGlossaryCandidates(request, glossary)

        chunks.forEach { chunk ->
            val prompt = promptBuilder.build(
                title = request.title,
                paragraphs = chunk.paragraphs,
                targetLanguage = request.targetLanguage,
                glossary = glossary,
            )
            val result = provider.translate(
                TranslationProviderRequest(
                    title = request.title,
                    prompt = prompt,
                    targetLanguage = request.targetLanguage,
                    paragraphCount = chunk.paragraphs.size,
                )
            )
            translatedTitle = result.title
            providerName = result.provider
            model = result.model
            inputTokens += result.inputTokens
            outputTokens += result.outputTokens
            translatedParagraphs += result.paragraphs
        }

        return TranslationTaskResult(
            chapterId = request.chapterId,
            language = request.targetLanguage,
            title = translatedTitle,
            paragraphs = translatedParagraphs,
            provider = providerName,
            model = model,
            inputTokens = inputTokens,
            outputTokens = outputTokens,
            estimatedCost = estimateOpenAICost(inputTokens, outputTokens),
            status = TranslationTaskStatus.SUCCEEDED,
            publicationStatus = PublicationStatus.PUBLISHED,
        )
    }

    private fun createPendingGlossaryCandidates(
        request: TranslateChapterRequest,
        glossary: List<GlossaryTerm>,
    ) {
        val confirmedTerms = glossary.map { it.sourceTerm }.toSet()
        val existingPendingTerms = glossaryService.pendingTerms(request.bookId).map { it.sourceTerm }.toSet()
        val ignoredTerms = confirmedTerms + existingPendingTerms

        extractRepeatedChineseTerms(request.paragraphs)
            .filterNot { candidate ->
                ignoredTerms.any {
                    candidate.sourceTerm == it || candidate.sourceTerm in it || it in candidate.sourceTerm
                }
            }
            .forEach { candidate ->
                glossaryService.createPendingTerm(
                    CreatePendingGlossaryTermRequest(
                        bookId = request.bookId,
                        chapterId = request.chapterId,
                        sourceTerm = candidate.sourceTerm,
                        suggestedTranslation = null,
                        occurrenceCount = candidate.occurrenceCount,
                    )
                )
            }
    }

    private fun extractRepeatedChineseTerms(paragraphs: List<String>): List<PendingGlossaryCandidate> {
        val text = paragraphs.joinToString("\n")
        val counts = linkedMapOf<String, Int>()
        Regex("[\\u4e00-\\u9fff]+")
            .findAll(text)
            .map { it.value }
            .forEach { segment ->
                for (length in 2..4) {
                    if (segment.length < length) continue
                    for (index in 0..(segment.length - length)) {
                        val candidate = segment.substring(index, index + length)
                        counts[candidate] = (counts[candidate] ?: 0) + 1
                    }
                }
            }

        return counts
            .filterValues { it > 1 }
            .map { PendingGlossaryCandidate(sourceTerm = it.key, occurrenceCount = it.value) }
            .sortedWith(compareByDescending<PendingGlossaryCandidate> { it.occurrenceCount }.thenBy { it.sourceTerm.length })
    }
}

private data class PendingGlossaryCandidate(
    val sourceTerm: String,
    val occurrenceCount: Int,
)

class FakeTranslationProvider(
    private val translatedParagraphs: List<String>,
    private val inputTokens: Int,
    private val outputTokens: Int,
) : TranslationProvider {
    var lastPrompt: String? = null
        private set

    override fun translate(request: TranslationProviderRequest): TranslationProviderResult {
        lastPrompt = request.prompt
        return TranslationProviderResult(
            title = "Chapter 1: Azure Cloud Sect",
            paragraphs = translatedParagraphs,
            provider = "openai",
            model = "gpt-5-mini",
            inputTokens = inputTokens,
            outputTokens = outputTokens,
        )
    }
}
