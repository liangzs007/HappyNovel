package com.happynovel.app

import com.happynovel.content.InMemoryContentRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AppApiControllerTest {
    private val controller = AppApiController(InMemoryContentRepository.withSeedData())

    @Test
    fun `home endpoint returns published sections`() {
        val response = controller.home()

        assertEquals("HappyNovel", response.appName)
        assertTrue(response.latestUpdates.isNotEmpty())
        assertTrue(response.popular.isNotEmpty())
        assertTrue(response.newBooks.isNotEmpty())
    }

    @Test
    fun `categories endpoint returns category filters`() {
        val response = controller.categories()

        assertTrue(response.categories.any { it.slug == "fantasy" })
        assertTrue(response.statuses.contains("ongoing"))
    }

    @Test
    fun `book detail endpoint returns catalog summary`() {
        val response = controller.bookDetail("book-seed-1")

        assertEquals("book-seed-1", response.id)
        assertEquals("Dragon Gate", response.title)
        assertEquals(2, response.chapterCount)
        assertNotNull(response.latestChapter)
    }

    @Test
    fun `chapter catalog endpoint returns ordered chapters`() {
        val response = controller.chapterCatalog("book-seed-1")

        assertEquals("book-seed-1", response.bookId)
        assertEquals(listOf(1, 2), response.chapters.map { it.order })
    }

    @Test
    fun `chapter content endpoint returns published translated paragraphs`() {
        val response = controller.chapterContent("chapter-seed-1")

        assertEquals("chapter-seed-1", response.id)
        assertEquals("en", response.language)
        assertTrue(response.paragraphs.first().contains("Azure Cloud"))
    }

    @Test
    fun `app config endpoints expose ads and compliance metadata`() {
        val adConfig = controller.adConfig()
        val compliance = controller.complianceConfig()

        assertEquals(true, adConfig.enabled)
        assertEquals(true, adConfig.readerBannerEnabled)
        assertEquals(5, adConfig.interstitialEveryChapters)
        assertEquals("HappyNovel Privacy Policy", compliance.privacyPolicyTitle)
        assertEquals("HappyNovel Terms of Service", compliance.termsTitle)
        assertEquals(true, compliance.adDisclosureEnabled)
    }
}
