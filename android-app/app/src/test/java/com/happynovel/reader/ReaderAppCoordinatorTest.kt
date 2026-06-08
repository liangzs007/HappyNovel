package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderAppCoordinatorTest {
    @Test
    fun `load home maps remote response into home state`() {
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = InMemoryReaderLocalRepository(),
        )

        val state = coordinator.loadHome()

        assertEquals("HappyNovel", state.appName)
        assertEquals("Dragon Gate", state.recommended.single().title)
    }

    @Test
    fun `load categories maps category filters and statuses`() {
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = InMemoryReaderLocalRepository(),
        )

        val state = coordinator.loadCategories()

        assertEquals("Fantasy", state.categories.single().name)
        assertEquals(listOf("ongoing", "completed"), state.statuses)
    }

    @Test
    fun `load book detail includes local bookshelf status`() {
        val localRepository = InMemoryReaderLocalRepository()
        localRepository.saveBook(BookSummary("book-seed-1", "Dragon Gate", "Chapter 1"))
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )

        val state = coordinator.loadBookDetail("book-seed-1")

        assertEquals("Dragon Gate", state.book.title)
        assertTrue(state.isInBookshelf)
        assertEquals("Chapter 1: Azure Cloud Sect", state.book.latestChapter?.title)
    }

    @Test
    fun `load chapter catalog marks current progress chapter`() {
        val localRepository = InMemoryReaderLocalRepository()
        localRepository.updateProgress(ReadingProgress("book-seed-1", "chapter-seed-1", 0.3f))
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )

        val state = coordinator.loadChapterCatalog("book-seed-1")

        assertEquals("book-seed-1", state.bookId)
        assertEquals("chapter-seed-1", state.currentChapterId)
        assertEquals("Chapter 1: Azure Cloud Sect", state.chapters.single().title)
    }

    @Test
    fun `start reading saves book progress and caches chapter`() {
        val localRepository = InMemoryReaderLocalRepository()
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )

        val chapter = coordinator.startReading("book-seed-1", "chapter-seed-1")

        assertEquals("Chapter 1: Azure Cloud Sect", chapter.title)
        assertTrue(localRepository.bookshelf().isSaved("book-seed-1"))
        assertEquals("chapter-seed-1", localRepository.bookshelf().progressFor("book-seed-1")?.chapterId)
        assertTrue(localRepository.chapterCache().hasChapter("chapter-seed-1"))
    }

    @Test
    fun `save book to bookshelf does not create reading progress`() {
        val localRepository = InMemoryReaderLocalRepository()
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )

        coordinator.saveBookToBookshelf("book-seed-1")

        assertTrue(localRepository.bookshelf().isSaved("book-seed-1"))
        assertEquals(null, localRepository.bookshelf().progressFor("book-seed-1"))
    }

    @Test
    fun `reader state exposes cached chapter settings and progress`() {
        val localRepository = InMemoryReaderLocalRepository()
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )
        coordinator.startReading("book-seed-1", "chapter-seed-1")
        localRepository.updateSettings(ReaderSettings.default().withFontSize(21))

        val state = coordinator.readerState("book-seed-1", "chapter-seed-1")

        assertEquals("Chapter 1: Azure Cloud Sect", state.chapter?.title)
        assertEquals(21, state.settings.fontSizeSp)
        assertEquals("chapter-seed-1", state.progress?.chapterId)
    }

    @Test
    fun `update reading progress clamps and persists percent`() {
        val localRepository = InMemoryReaderLocalRepository()
        val coordinator = ReaderAppCoordinator(
            remoteDataSource = FakeReaderRemoteDataSource(),
            localRepository = localRepository,
        )
        coordinator.startReading("book-seed-1", "chapter-seed-1")

        coordinator.updateReadingProgress("book-seed-1", "chapter-seed-1", 1.4f)

        val state = coordinator.readerState("book-seed-1", "chapter-seed-1")
        assertEquals(1f, state.progress?.percent)
    }
}

private class FakeReaderRemoteDataSource : ReaderRemoteDataSource {
    override fun home(): AppHomeResponseDto = AppHomeResponseDto(
        appName = "HappyNovel",
        recommended = listOf(book),
        latestUpdates = listOf(book),
        popular = listOf(book),
        newBooks = listOf(book),
    )

    override fun categories(): AppCategoriesResponseDto = AppCategoriesResponseDto(
        categories = listOf(AppCategoryDto("category-fantasy", "Fantasy", "fantasy")),
        statuses = listOf("ongoing", "completed"),
    )

    override fun bookDetail(bookId: String): AppBookDetailDto = AppBookDetailDto(
        id = book.id,
        title = book.title,
        author = book.author,
        coverUrl = book.coverUrl,
        description = book.description,
        status = book.status,
        categories = listOf(AppCategoryDto("category-fantasy", "Fantasy", "fantasy")),
        chapterCount = 1,
        latestChapter = AppChapterSummaryDto("chapter-seed-1", 1, "Chapter 1: Azure Cloud Sect", "2026-06-08T00:00:00Z"),
    )

    override fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto = AppChapterCatalogResponseDto(
        bookId = bookId,
        chapters = listOf(AppChapterSummaryDto("chapter-seed-1", 1, "Chapter 1: Azure Cloud Sect", "2026-06-08T00:00:00Z")),
    )

    override fun chapterContent(chapterId: String): AppChapterContentDto = AppChapterContentDto(
        id = chapterId,
        bookId = book.id,
        title = "Chapter 1: Azure Cloud Sect",
        language = "en",
        paragraphs = listOf("The morning bell echoed across Azure Cloud Sect."),
    )

    private val book = AppBookSummaryDto(
        id = "book-seed-1",
        title = "Dragon Gate",
        author = "Happy Novel Team",
        coverUrl = "https://example.com/covers/dragon-gate.jpg",
        description = "A translated cultivation novel prepared for MVP API validation.",
        status = "ongoing",
        latestChapterTitle = "Chapter 1: Azure Cloud Sect",
        updatedAt = "2026-06-08T00:00:00Z",
    )
}
