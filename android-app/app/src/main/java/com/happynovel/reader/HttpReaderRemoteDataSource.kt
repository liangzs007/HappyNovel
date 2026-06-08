package com.happynovel.reader

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface HttpTextClient {
    fun get(url: String): String

    fun post(url: String, body: String): String
}

class UrlConnectionHttpTextClient : HttpTextClient {
    override fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    override fun post(url: String, body: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.outputStream.bufferedWriter().use { it.write(body) }
        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}

class HttpReaderRemoteDataSource(
    private val routes: HappyNovelApiRoutes,
    private val client: HttpTextClient = UrlConnectionHttpTextClient(),
) : ReaderRemoteDataSource {
    override fun home(): AppHomeResponseDto = parseHome(client.get(routes.home()))

    override fun categories(): AppCategoriesResponseDto = parseCategories(client.get(routes.categories()))

    override fun books(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): AppBookListResponseDto = parseBookList(client.get(routes.books(category, status, sort, limit)))

    override fun bookDetail(bookId: String): AppBookDetailDto = parseBookDetail(client.get(routes.bookDetail(bookId)))

    override fun chapterCatalog(bookId: String): AppChapterCatalogResponseDto =
        parseChapterCatalog(client.get(routes.chapterCatalog(bookId)))

    override fun chapterContent(chapterId: String): AppChapterContentDto =
        parseChapterContent(client.get(routes.chapterContent(chapterId)))

    override fun adConfig(): AppAdConfigDto = parseAdConfig(client.get(routes.adConfig()))

    override fun complianceConfig(): AppComplianceConfigDto = parseComplianceConfig(client.get(routes.complianceConfig()))

    override fun createAnonymousDevice(): AppAnonymousDeviceDto =
        parseAnonymousDevice(client.post(routes.anonymousDevice(), "{}"))

    override fun recordReadingEvent(request: AppReadingEventRequestDto): AppReadingEventDto =
        parseReadingEvent(client.post(routes.readingEvents(), request.toJson().toString()))

    private fun parseHome(json: String): AppHomeResponseDto {
        val root = JSONObject(json)
        return AppHomeResponseDto(
            appName = root.getString("appName"),
            recommended = root.getJSONArray("recommended").mapObjects { it.toBookSummaryDto() },
            latestUpdates = root.getJSONArray("latestUpdates").mapObjects { it.toBookSummaryDto() },
            popular = root.getJSONArray("popular").mapObjects { it.toBookSummaryDto() },
            newBooks = root.getJSONArray("newBooks").mapObjects { it.toBookSummaryDto() },
        )
    }

    private fun parseCategories(json: String): AppCategoriesResponseDto {
        val root = JSONObject(json)
        return AppCategoriesResponseDto(
            categories = root.getJSONArray("categories").mapObjects { it.toCategoryDto() },
            statuses = root.getJSONArray("statuses").mapStrings(),
        )
    }

    private fun parseBookList(json: String): AppBookListResponseDto {
        val root = JSONObject(json)
        return AppBookListResponseDto(
            books = root.getJSONArray("books").mapObjects { it.toBookSummaryDto() },
        )
    }

    private fun parseBookDetail(json: String): AppBookDetailDto = JSONObject(json).toBookDetailDto()

    private fun parseChapterCatalog(json: String): AppChapterCatalogResponseDto {
        val root = JSONObject(json)
        return AppChapterCatalogResponseDto(
            bookId = root.getString("bookId"),
            chapters = root.getJSONArray("chapters").mapObjects { it.toChapterSummaryDto() },
        )
    }

    private fun parseChapterContent(json: String): AppChapterContentDto {
        val root = JSONObject(json)
        return AppChapterContentDto(
            id = root.getString("id"),
            bookId = root.getString("bookId"),
            title = root.getString("title"),
            language = root.getString("language"),
            paragraphs = root.getJSONArray("paragraphs").mapStrings(),
        )
    }

    private fun parseAdConfig(json: String): AppAdConfigDto {
        val root = JSONObject(json)
        return AppAdConfigDto(
            enabled = root.getBoolean("enabled"),
            readerBannerEnabled = root.getBoolean("readerBannerEnabled"),
            interstitialEveryChapters = root.getInt("interstitialEveryChapters"),
        )
    }

    private fun parseComplianceConfig(json: String): AppComplianceConfigDto {
        val root = JSONObject(json)
        return AppComplianceConfigDto(
            privacyPolicyTitle = root.getString("privacyPolicyTitle"),
            termsTitle = root.getString("termsTitle"),
            adDisclosureEnabled = root.getBoolean("adDisclosureEnabled"),
            adDisclosureText = root.getString("adDisclosureText"),
        )
    }

    private fun parseAnonymousDevice(json: String): AppAnonymousDeviceDto {
        val root = JSONObject(json)
        return AppAnonymousDeviceDto(deviceId = root.getString("deviceId"))
    }

    private fun parseReadingEvent(json: String): AppReadingEventDto {
        val root = JSONObject(json)
        return AppReadingEventDto(
            deviceId = root.getString("deviceId"),
            bookId = root.getString("bookId"),
            chapterId = root.getString("chapterId"),
            percent = root.getDouble("percent").toFloat(),
        )
    }

    private fun AppReadingEventRequestDto.toJson(): JSONObject = JSONObject()
        .put("deviceId", deviceId)
        .put("bookId", bookId)
        .put("chapterId", chapterId)
        .put("percent", percent.toDouble())

    private fun JSONObject.toBookSummaryDto(): AppBookSummaryDto = AppBookSummaryDto(
        id = getString("id"),
        title = getString("title"),
        author = getString("author"),
        coverUrl = getString("coverUrl"),
        description = getString("description"),
        status = getString("status"),
        latestChapterTitle = getString("latestChapterTitle"),
        updatedAt = getString("updatedAt"),
    )

    private fun JSONObject.toCategoryDto(): AppCategoryDto = AppCategoryDto(
        id = getString("id"),
        name = getString("name"),
        slug = getString("slug"),
    )

    private fun JSONObject.toChapterSummaryDto(): AppChapterSummaryDto = AppChapterSummaryDto(
        id = getString("id"),
        order = getInt("order"),
        title = getString("title"),
        updatedAt = getString("updatedAt"),
    )

    private fun JSONObject.toBookDetailDto(): AppBookDetailDto = AppBookDetailDto(
        id = getString("id"),
        title = getString("title"),
        author = getString("author"),
        coverUrl = getString("coverUrl"),
        description = getString("description"),
        status = getString("status"),
        categories = getJSONArray("categories").mapObjects { it.toCategoryDto() },
        chapterCount = getInt("chapterCount"),
        latestChapter = optJSONObject("latestChapter")?.toChapterSummaryDto(),
    )

    private fun JSONArray.mapStrings(): List<String> = List(length()) { index -> getString(index) }

    private fun <T> JSONArray.mapObjects(transform: (JSONObject) -> T): List<T> =
        List(length()) { index -> transform(getJSONObject(index)) }
}
