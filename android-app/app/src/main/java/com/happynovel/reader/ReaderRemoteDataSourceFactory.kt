package com.happynovel.reader

object ReaderRemoteDataSourceFactory {
    fun create(
        baseUrl: String,
        httpClient: HttpTextClient = UrlConnectionHttpTextClient(),
    ): ReaderRemoteDataSource {
        val trimmedBaseUrl = baseUrl.trim()
        if (trimmedBaseUrl.isEmpty()) {
            return SeedReaderRemoteDataSource()
        }
        return HttpReaderRemoteDataSource(
            routes = HappyNovelApiRoutes(trimmedBaseUrl),
            client = httpClient,
        )
    }
}
