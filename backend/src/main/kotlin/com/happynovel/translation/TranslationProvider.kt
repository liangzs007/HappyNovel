package com.happynovel.translation

import java.math.BigDecimal

data class TranslationProviderRequest(
    val title: String,
    val prompt: String,
    val targetLanguage: String,
    val paragraphCount: Int,
)

data class TranslationProviderResult(
    val title: String,
    val paragraphs: List<String>,
    val provider: String,
    val model: String,
    val inputTokens: Int,
    val outputTokens: Int,
)

interface TranslationProvider {
    fun translate(request: TranslationProviderRequest): TranslationProviderResult
}

fun estimateOpenAICost(inputTokens: Int, outputTokens: Int): BigDecimal {
    val inputCost = BigDecimal(inputTokens).multiply(BigDecimal("0.00000025"))
    val outputCost = BigDecimal(outputTokens).multiply(BigDecimal("0.000002"))
    return inputCost + outputCost
}
