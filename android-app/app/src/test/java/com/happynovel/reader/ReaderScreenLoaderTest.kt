package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderScreenLoaderTest {
    @Test
    fun `home loader returns content state when books are available`() {
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(SeedReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
        )

        val state = loader.home()

        assertFalse(state.isLoading)
        assertEquals("Dragon Gate", state.content?.sections?.first()?.books?.single()?.title)
    }

    @Test
    fun `home loader returns empty state when no books are available`() {
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(EmptyReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
        )

        val state = loader.home()

        assertEquals("No books found.", state.message)
    }

    @Test
    fun `home loader converts remote failures to retryable english error`() {
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(FailingReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
        )

        val state = loader.home()

        assertTrue(state.shouldShowRetry)
        assertEquals("Unable to load books. Try again.", state.message)
    }

    @Test
    fun `book list loader returns filtered books`() {
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(SeedReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
        )

        val state = loader.books(category = "fantasy", status = "ongoing", sort = "popular", limit = 12)

        assertEquals("Dragon Gate", state.content?.books?.single()?.title)
    }

    @Test
    fun `bookshelf loader returns local saved books`() {
        val localRepository = InMemoryReaderLocalRepository()
        localRepository.saveBook(BookSummary("book-seed-1", "Dragon Gate", "Chapter 1"))
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(SeedReaderRemoteDataSource(), localRepository),
        )

        val state = loader.bookshelf()

        assertEquals("Dragon Gate", state.content?.books?.single()?.title)
    }

    @Test
    fun `reader loader returns unavailable empty state when chapter is missing`() {
        val loader = ReaderScreenLoader(
            coordinator = ReaderAppCoordinator(SeedReaderRemoteDataSource(), InMemoryReaderLocalRepository()),
        )

        val state = loader.reader("book-seed-1", "chapter-missing")

        assertEquals("This chapter is unavailable.", state.message)
    }
}

private class EmptyReaderRemoteDataSource : ReaderRemoteDataSource {
    override fun home(): AppHomeResponseDto = AppHomeResponseDto(
        appName = "HappyNovel",
        recommended = emptyList(),
        latestUpdates = emptyList(),
        popular = emptyList(),
        newBooks = emptyList(),
    )

    override fun categories(): AppCategoriesResponseDto = SeedReaderRemoteDataSource().categories()

    override fun books(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): AppBookListResponseDto = AppBookListResponseDto(emptyList())

    override fun bookDetail(bookId: String): AppBookDetailDto = SeedReaderRemoteDataSource().bookDetail(bookId)

    override fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto =
        SeedReaderRemoteDataSource().chapterCatalog(bookId)

    override fun chapterContent(chapterId: String): AppChapterContentDto =
        SeedReaderRemoteDataSource().chapterContent(chapterId)
}

private class FailingReaderRemoteDataSource : ReaderRemoteDataSource {
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
