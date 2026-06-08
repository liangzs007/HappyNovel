package com.happynovel.translation

data class ChapterChunk(
    val index: Int,
    val paragraphs: List<String>,
)

class ChapterChunker(
    private val maxCharacters: Int,
) {
    fun chunk(paragraphs: List<String>): List<ChapterChunk> {
        val chunks = mutableListOf<ChapterChunk>()
        var current = mutableListOf<String>()
        var currentLength = 0

        paragraphs.forEach { paragraph ->
            val nextLength = currentLength + paragraph.length
            if (current.isNotEmpty() && nextLength > maxCharacters) {
                chunks += ChapterChunk(index = chunks.size, paragraphs = current)
                current = mutableListOf()
                currentLength = 0
            }
            current += paragraph
            currentLength += paragraph.length
        }

        if (current.isNotEmpty()) {
            chunks += ChapterChunk(index = chunks.size, paragraphs = current)
        }

        return chunks
    }
}
