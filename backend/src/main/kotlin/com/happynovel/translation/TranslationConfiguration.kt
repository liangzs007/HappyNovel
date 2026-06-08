package com.happynovel.translation

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class NotConfiguredOpenAIHttpClient : OpenAIHttpClient {
    override fun createResponse(apiKey: String, model: String, prompt: String): OpenAIProviderRawResponse {
        error("OpenAI HTTP client is not configured for live calls")
    }
}

@Configuration
class TranslationConfiguration {
    @Bean
    fun chapterChunker(): ChapterChunker = ChapterChunker(maxCharacters = 6000)

    @Bean
    fun translationPromptBuilder(): TranslationPromptBuilder = TranslationPromptBuilder()

    @Bean
    fun openAIHttpClient(): OpenAIHttpClient = NotConfiguredOpenAIHttpClient()

    @Bean
    fun translationProvider(
        @Value("\${openai.api-key:}") apiKey: String,
        @Value("\${openai.model:gpt-5-mini}") model: String,
        httpClient: OpenAIHttpClient,
    ): TranslationProvider = OpenAITranslationProvider(
        apiKey = apiKey,
        model = model,
        httpClient = httpClient,
    )

    @Bean
    fun translationPipelineService(
        glossaryService: GlossaryService,
        translationProvider: TranslationProvider,
        chapterChunker: ChapterChunker,
        promptBuilder: TranslationPromptBuilder,
    ): TranslationPipelineService = TranslationPipelineService(
        glossaryService = glossaryService,
        provider = translationProvider,
        chunker = chapterChunker,
        promptBuilder = promptBuilder,
    )
}
