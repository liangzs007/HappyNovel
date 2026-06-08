package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.json.JSONObject
import org.junit.Test

class HttpReaderRemoteDataSourceTest {
    @Test
    fun `http remote data source requests backend routes and parses responses`() {
        val client = FakeHttpTextClient(
            getResponses = mapOf(
                "https://api.example.test/api/app/home" to homeJson,
                "https://api.example.test/api/app/categories" to categoriesJson,
                "https://api.example.test/api/app/books?category=fantasy&status=ongoing&sort=popular&limit=12" to bookListJson,
                "https://api.example.test/api/app/books/book-seed-1" to detailJson,
                "https://api.example.test/api/app/books/book-seed-1/chapters" to catalogJson,
                "https://api.example.test/api/app/chapters/chapter-seed-1" to chapterJson,
                "https://api.example.test/api/app/ad-config" to adConfigJson,
                "https://api.example.test/api/app/compliance-config" to complianceConfigJson,
            ),
            postResponses = mapOf(
                "https://api.example.test/api/app/devices/anonymous" to anonymousDeviceJson,
                "https://api.example.test/api/app/reading-events" to readingEventJson,
            ),
        )
        val dataSource = HttpReaderRemoteDataSource(
            routes = HappyNovelApiRoutes("https://api.example.test"),
            client = client,
        )

        assertEquals("Dragon Gate", dataSource.home().recommended.single().title)
        assertEquals("Fantasy", dataSource.categories().categories.single().name)
        assertEquals("Dragon Gate", dataSource.books("fantasy", "ongoing", "popular", 12).books.single().title)
        assertEquals("Dragon Gate", dataSource.bookDetail("book-seed-1").title)
        assertEquals("chapter-seed-1", dataSource.chapterCatalog("book-seed-1").chapters.single().id)
        assertTrue(dataSource.chapterContent("chapter-seed-1").paragraphs.first().contains("Azure Cloud"))
        assertEquals(5, dataSource.adConfig().interstitialEveryChapters)
        assertEquals("HappyNovel Privacy Policy", dataSource.complianceConfig().privacyPolicyTitle)
        assertEquals("anon-device-1", dataSource.createAnonymousDevice().deviceId)
        assertEquals(
            0.42f,
            dataSource.recordReadingEvent(
                AppReadingEventRequestDto(
                    deviceId = "anon-device-1",
                    bookId = "book-seed-1",
                    chapterId = "chapter-seed-1",
                    percent = 0.42f,
                ),
            ).percent,
        )
        val postedEvent = JSONObject(client.postBodies.getValue("https://api.example.test/api/app/reading-events"))
        assertEquals("anon-device-1", postedEvent.getString("deviceId"))
        assertEquals(0.42, postedEvent.getDouble("percent"), 0.0001)
    }
}

class FakeHttpTextClient(
    private val getResponses: Map<String, String>,
    private val postResponses: Map<String, String> = emptyMap(),
) : HttpTextClient {
    val postBodies = mutableMapOf<String, String>()

    override fun get(url: String): String = getResponses.getValue(url)

    override fun post(url: String, body: String): String {
        postBodies[url] = body
        return postResponses.getValue(url)
    }
}

const val bookJson = """
{
  "id": "book-seed-1",
  "title": "Dragon Gate",
  "author": "Happy Novel Team",
  "coverUrl": "https://example.com/covers/dragon-gate.jpg",
  "description": "A translated cultivation novel prepared for MVP API validation.",
  "status": "ongoing",
  "latestChapterTitle": "Chapter 1: Azure Cloud Sect",
  "updatedAt": "2026-06-08T00:00:00Z"
}
"""

const val homeJson = """
{
  "appName": "HappyNovel",
  "recommended": [$bookJson],
  "latestUpdates": [$bookJson],
  "popular": [$bookJson],
  "newBooks": [$bookJson]
}
"""

const val categoriesJson = """
{
  "categories": [
    {"id": "category-fantasy", "name": "Fantasy", "slug": "fantasy"}
  ],
  "statuses": ["ongoing", "completed"]
}
"""

const val bookListJson = """
{
  "books": [$bookJson]
}
"""

const val detailJson = """
{
  "id": "book-seed-1",
  "title": "Dragon Gate",
  "author": "Happy Novel Team",
  "coverUrl": "https://example.com/covers/dragon-gate.jpg",
  "description": "A translated cultivation novel prepared for MVP API validation.",
  "status": "ongoing",
  "categories": [
    {"id": "category-fantasy", "name": "Fantasy", "slug": "fantasy"}
  ],
  "chapterCount": 1,
  "latestChapter": {"id": "chapter-seed-1", "order": 1, "title": "Chapter 1: Azure Cloud Sect", "updatedAt": "2026-06-08T00:00:00Z"}
}
"""

const val catalogJson = """
{
  "bookId": "book-seed-1",
  "chapters": [
    {"id": "chapter-seed-1", "order": 1, "title": "Chapter 1: Azure Cloud Sect", "updatedAt": "2026-06-08T00:00:00Z"}
  ]
}
"""

const val chapterJson = """
{
  "id": "chapter-seed-1",
  "bookId": "book-seed-1",
  "title": "Chapter 1: Azure Cloud Sect",
  "language": "en",
  "paragraphs": ["The morning bell echoed across Azure Cloud Sect."]
}
"""

const val adConfigJson = """
{
  "enabled": true,
  "readerBannerEnabled": true,
  "interstitialEveryChapters": 5
}
"""

const val complianceConfigJson = """
{
  "privacyPolicyTitle": "HappyNovel Privacy Policy",
  "termsTitle": "HappyNovel Terms of Service",
  "adDisclosureEnabled": true,
  "adDisclosureText": "This app may show ads to support translated novel reading."
}
"""

const val anonymousDeviceJson = """
{
  "deviceId": "anon-device-1"
}
"""

const val readingEventJson = """
{
  "deviceId": "anon-device-1",
  "bookId": "book-seed-1",
  "chapterId": "chapter-seed-1",
  "percent": 0.42
}
"""
