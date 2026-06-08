package com.happynovel.translation

interface OpenAIHttpClient {
    fun createResponse(apiKey: String, model: String, prompt: String): OpenAIProviderRawResponse
}

data class OpenAIProviderRawResponse(
    val text: String,
    val inputTokens: Int,
    val outputTokens: Int,
)

class CapturingOpenAIHttpClient(
    private val response: OpenAIProviderRawResponse,
) : OpenAIHttpClient {
    var capturedApiKey: String? = null
        private set
    var capturedModel: String? = null
        private set
    var capturedPrompt: String? = null
        private set

    override fun createResponse(apiKey: String, model: String, prompt: String): OpenAIProviderRawResponse {
        capturedApiKey = apiKey
        capturedModel = model
        capturedPrompt = prompt
        return response
    }
}

class OpenAITranslationProvider(
    private val apiKey: String,
    private val model: String,
    private val httpClient: OpenAIHttpClient,
) : TranslationProvider {
    override fun translate(request: TranslationProviderRequest): TranslationProviderResult {
        val raw = httpClient.createResponse(apiKey, model, request.prompt)
        return TranslationProviderResult(
            title = extractTitle(raw.text) ?: request.title,
            paragraphs = extractParagraphs(raw.text).ifEmpty { listOf(raw.text) },
            provider = "openai",
            model = model,
            inputTokens = raw.inputTokens,
            outputTokens = raw.outputTokens,
        )
    }

    private fun extractTitle(json: String): String? = Regex("\\\"title\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
        .find(json)
        ?.groupValues
        ?.get(1)

    private fun extractParagraphs(json: String): List<String> {
        val arrayContent = Regex("\\\"paragraphs\\\"\\s*:\\s*\\[(.*)]", RegexOption.DOT_MATCHES_ALL)
            .find(json)
            ?.groupValues
            ?.get(1)
            ?: return emptyList()

        return Regex("\\\"([^\\\"]*)\\\"")
            .findAll(arrayContent)
            .map { it.groupValues[1] }
            .toList()
    }
}
