package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeedReaderRemoteDataSourceTest {
    @Test
    fun `seed data source exposes backend shaped reader data`() {
        val dataSource = SeedReaderRemoteDataSource()

        assertEquals("HappyNovel", dataSource.home().appName)
        assertEquals("Dragon Gate", dataSource.home().recommended.single().title)
        assertEquals("Fantasy", dataSource.categories().categories.single().name)
        assertEquals("Chapter 1: Azure Cloud Sect", dataSource.bookDetail("book-seed-1").latestChapter?.title)
        assertEquals("chapter-seed-1", dataSource.chapterCatalog("book-seed-1").chapters.first().id)
        assertTrue(dataSource.chapterContent("chapter-seed-1").paragraphs.first().contains("Azure Cloud"))
    }

    @Test
    fun `launch state composes primary screens from coordinator`() {
        val launchState = ReaderLaunchStateFactory.create()

        assertEquals("HappyNovel", launchState.home.title)
        assertEquals("Categories", launchState.categories.title)
        assertEquals("Dragon Gate", launchState.bookDetail.title)
        assertEquals("Chapters", launchState.chapterCatalog.title)
        assertEquals("Chapter 1: Azure Cloud Sect", launchState.reader.title)
    }
}
