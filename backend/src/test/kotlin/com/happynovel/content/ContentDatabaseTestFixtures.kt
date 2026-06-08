package com.happynovel.content

class FakeContentDatabaseClient : ContentDatabaseClient {
    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        return when {
            sql.contains("from taxonomy_category") -> listOf(
                mapOf(
                    "id" to "category-fantasy",
                    "name" to "Fantasy",
                    "slug" to "fantasy",
                ),
            )

            sql.contains("from book b") -> listOf(
                mapOf(
                    "id" to "book-seed-1",
                    "title" to "Dragon Gate",
                    "author" to "Happy Novel Team",
                    "cover_url" to "https://example.com/covers/dragon-gate.jpg",
                    "description" to "A translated cultivation novel.",
                    "serialization_status" to "ongoing",
                    "latest_chapter_title" to "Chapter 1: Azure Cloud Sect",
                    "updated_at" to "2026-06-08T00:00:00Z",
                ),
            )

            sql.contains("ct.paragraphs") -> listOf(
                mapOf(
                    "id" to "chapter-seed-1",
                    "book_id" to "book-seed-1",
                    "title" to "Chapter 1: Azure Cloud Sect",
                    "language" to "en",
                    "paragraphs" to """["The morning bell echoed across Azure Cloud Sect."]""",
                ),
            )

            sql.contains("from chapter") -> listOf(
                mapOf(
                    "id" to "chapter-seed-1",
                    "chapter_order" to 1,
                    "title" to "Chapter 1: Azure Cloud Sect",
                    "updated_at" to "2026-06-08T00:00:00Z",
                ),
            )

            else -> emptyList()
        }
    }
}
