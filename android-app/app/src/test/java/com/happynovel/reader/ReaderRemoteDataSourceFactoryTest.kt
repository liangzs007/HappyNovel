package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderRemoteDataSourceFactoryTest {
    @Test
    fun `factory uses seed data source when base url is blank`() {
        val dataSource = ReaderRemoteDataSourceFactory.create(
            baseUrl = "",
            httpClient = FakeHttpTextClient(emptyMap()),
        )

        assertTrue(dataSource is SeedReaderRemoteDataSource)
    }

    @Test
    fun `factory uses http data source when base url is configured`() {
        val dataSource = ReaderRemoteDataSourceFactory.create(
            baseUrl = "https://api.example.test",
            httpClient = FakeHttpTextClient(
                mapOf(
                    "https://api.example.test/api/app/home" to homeJson,
                    "https://api.example.test/api/app/categories" to categoriesJson,
                    "https://api.example.test/api/app/books/book-seed-1" to detailJson,
                    "https://api.example.test/api/app/books/book-seed-1/chapters" to catalogJson,
                    "https://api.example.test/api/app/chapters/chapter-seed-1" to chapterJson,
                ),
            ),
        )

        assertTrue(dataSource is HttpReaderRemoteDataSource)
        assertEquals("Dragon Gate", dataSource.home().recommended.single().title)
    }
}
