package com.happynovel.content

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContentRepositoryFactoryTest {
    @Test
    fun `defaults to seed repository when jdbc mode is disabled`() {
        val repository = ContentRepositoryFactory.create(
            mode = ContentRepositoryMode.SEED,
            databaseClient = FakeContentDatabaseClient(),
        )

        assertEquals("Dragon Gate", repository.homeBooks().single().title)
    }

    @Test
    fun `uses jdbc repository when jdbc mode is enabled`() {
        val repository = ContentRepositoryFactory.create(
            mode = ContentRepositoryMode.JDBC,
            databaseClient = FakeContentDatabaseClient(),
        )

        assertEquals("Dragon Gate", repository.homeBooks().single().title)
        assertEquals("Fantasy", repository.categories().single().name)
    }
}
