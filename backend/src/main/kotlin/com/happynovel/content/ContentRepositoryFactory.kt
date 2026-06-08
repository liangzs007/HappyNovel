package com.happynovel.content

enum class ContentRepositoryMode {
    SEED,
    JDBC,
}

object ContentRepositoryFactory {
    fun create(
        mode: ContentRepositoryMode,
        databaseClient: ContentDatabaseClient,
    ): ContentRepository = when (mode) {
        ContentRepositoryMode.SEED -> InMemoryContentRepository.withSeedData()
        ContentRepositoryMode.JDBC -> JdbcContentRepository(databaseClient)
    }
}

object MissingContentDatabaseClient : ContentDatabaseClient {
    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> {
        throw IllegalStateException("JDBC content repository requires a configured JdbcTemplate")
    }

    override fun update(sql: String, vararg args: Any?): Int {
        throw IllegalStateException("JDBC content repository requires a configured JdbcTemplate")
    }
}
