package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderLaunchTextModelTest {
    @Test
    fun `launch text model renders content from screen loader`() {
        val model = ReaderLaunchTextModelFactory.create(
            loader = ReaderScreenLoader(
                ReaderAppCoordinator(SeedReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
            ),
        )

        assertEquals("HappyNovel", model.title)
        assertEquals(listOf("Home", "Categories", "Bookshelf"), model.bottomTabs.map { it.label })
        assertEquals("Fantasy / ongoing / popular", model.bookListFilterLabel)
        assertTrue(model.sections.any { it.title == "Recommended" })
        assertTrue(model.sections.any { it.body.contains("Dragon Gate") })
        assertTrue(model.sections.any { it.title == "Book List" && it.body.contains("Dragon Gate") })
        assertTrue(model.sections.any { it.title == "Reader Preview" })
    }

    @Test
    fun `launch text model renders loader messages when screens fail`() {
        val model = ReaderLaunchTextModelFactory.create(
            loader = ReaderScreenLoader(
                ReaderAppCoordinator(FailingLaunchRemoteDataSource(), InMemoryReaderLocalRepository()),
            ),
        )

        assertEquals("HappyNovel", model.title)
        assertTrue(model.sections.any { it.body == "Unable to load books. Try again." })
        assertTrue(model.sections.any { it.body == "Unable to load categories. Try again." })
    }
}

private class FailingLaunchRemoteDataSource : ReaderRemoteDataSource {
    override fun home(): AppHomeResponseDto = throw IllegalStateException("network unavailable")

    override fun categories(): AppCategoriesResponseDto = throw IllegalStateException("network unavailable")

    override fun books(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): AppBookListResponseDto = throw IllegalStateException("network unavailable")

    override fun bookDetail(bookId: String): AppBookDetailDto = throw IllegalStateException("network unavailable")

    override fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto =
        throw IllegalStateException("network unavailable")

    override fun chapterContent(chapterId: String): AppChapterContentDto = throw IllegalStateException("network unavailable")
}
