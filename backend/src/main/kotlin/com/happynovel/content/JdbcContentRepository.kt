package com.happynovel.content

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.jdbc.core.JdbcTemplate
import java.time.OffsetDateTime

interface ContentDatabaseClient {
    fun query(sql: String, vararg args: Any?): List<Map<String, Any?>>

    fun update(sql: String, vararg args: Any?): Int
}

class JdbcTemplateContentDatabaseClient(
    private val jdbcTemplate: JdbcTemplate,
) : ContentDatabaseClient {
    override fun query(sql: String, vararg args: Any?): List<Map<String, Any?>> =
        jdbcTemplate.queryForList(sql, *args)

    override fun update(sql: String, vararg args: Any?): Int =
        jdbcTemplate.update(sql, *args)
}

class JdbcContentRepository(
    private val databaseClient: ContentDatabaseClient,
    private val language: String = "en",
) : ContentRepository {
    override fun homeBooks(): List<BookSummary> = latestBooks()

    override fun recommendedBooks(): List<BookSummary> =
        databaseClient.query("$PUBLISHED_BOOKS_SQL order by b.recommendation_weight desc, b.updated_at desc limit 20")
            .map(JdbcContentRowMapper::bookSummary)

    override fun latestBooks(): List<BookSummary> =
        databaseClient.query("$PUBLISHED_BOOKS_SQL order by b.updated_at desc limit 20")
            .map(JdbcContentRowMapper::bookSummary)

    override fun popularBooks(): List<BookSummary> =
        databaseClient.query(POPULAR_BOOKS_SQL).map(JdbcContentRowMapper::bookSummary)

    override fun newBooks(): List<BookSummary> =
        databaseClient.query("$PUBLISHED_BOOKS_SQL order by b.updated_at desc limit 20")
            .map(JdbcContentRowMapper::bookSummary)

    override fun browseBooks(
        category: String?,
        status: String?,
        sort: String?,
        limit: Int,
    ): List<BookSummary> {
        val conditions = mutableListOf<String>()
        val args = mutableListOf<Any>()
        if (!category.isNullOrBlank()) {
            conditions += """
                exists (
                    select 1
                    from book_category bc
                    join taxonomy_category tc on tc.id = bc.category_id
                    where bc.book_id = b.id
                      and tc.slug = ?
                      and tc.enabled = true
                )
            """.trimIndent()
            args += category
        }
        if (!status.isNullOrBlank()) {
            conditions += "b.serialization_status = ?"
            args += status
        }
        val whereClause = if (conditions.isEmpty()) {
            ""
        } else {
            conditions.joinToString(separator = " and ", prefix = " and ")
        }
        val orderBy = when (sort) {
            "popular" -> "order by b.recommendation_weight desc, b.updated_at desc"
            "newest" -> "order by b.updated_at desc"
            "recommended" -> "order by b.recommendation_weight desc, b.updated_at desc"
            else -> "order by b.updated_at desc"
        }
        return databaseClient.query(
            "$PUBLISHED_BOOKS_SQL$whereClause $orderBy limit ?",
            *args.toTypedArray(),
            limit.coerceIn(1, 50),
        ).map(JdbcContentRowMapper::bookSummary)
    }

    override fun categories(): List<Category> =
        databaseClient.query(CATEGORIES_SQL).map(JdbcContentRowMapper::category)

    override fun statuses(): List<String> = listOf("ongoing", "completed")

    override fun bookDetail(bookId: String): BookDetail {
        val book = databaseClient.query("$PUBLISHED_BOOKS_SQL and b.id = ?", bookId)
            .firstOrNull()
            ?.let(JdbcContentRowMapper::bookSummary)
            ?: throw NoSuchElementException("Book not found: $bookId")
        return JdbcContentRowMapper.bookDetail(
            book = book,
            categories = categories(),
            chapters = chapterCatalog(bookId),
        )
    }

    override fun chapterCatalog(bookId: String): List<ChapterSummary> =
        databaseClient.query(CHAPTER_CATALOG_SQL, bookId).map(JdbcContentRowMapper::chapterSummary)

    override fun chapterContent(chapterId: String): ChapterContent =
        databaseClient.query(CHAPTER_CONTENT_SQL, language, chapterId)
            .firstOrNull()
            ?.let(JdbcContentRowMapper::chapterContent)
            ?: throw NoSuchElementException("Chapter not found: $chapterId")

    companion object {
        private const val CATEGORIES_SQL = """
            select id, name, slug
            from taxonomy_category
            where enabled = true
            order by name
        """

        private const val PUBLISHED_BOOKS_SQL = """
            select
                b.id::text as id,
                b.title,
                b.author,
                coalesce(b.cover_url, '') as cover_url,
                coalesce(b.description, '') as description,
                b.serialization_status,
                coalesce(latest.title, '') as latest_chapter_title,
                b.updated_at
            from book b
            left join lateral (
                select ct.title
                from chapter c
                join chapter_translation ct on ct.chapter_id = c.id
                where c.book_id = b.id
                  and c.publication_status = 'published'
                  and ct.publication_status = 'published'
                order by c.chapter_order desc
                limit 1
            ) latest on true
            where b.publication_status = 'published'
        """

        private const val POPULAR_BOOKS_SQL = """
            select
                b.id::text as id,
                b.title,
                b.author,
                coalesce(b.cover_url, '') as cover_url,
                coalesce(b.description, '') as description,
                b.serialization_status,
                coalesce(latest.title, '') as latest_chapter_title,
                b.updated_at
            from book b
            left join lateral (
                select ct.title
                from chapter c
                join chapter_translation ct on ct.chapter_id = c.id
                where c.book_id = b.id
                  and c.publication_status = 'published'
                  and ct.publication_status = 'published'
                order by c.chapter_order desc
                limit 1
            ) latest on true
            left join reading_event re on re.book_id = b.id
            where b.publication_status = 'published'
            group by b.id, latest.title
            order by count(re.id) desc, b.updated_at desc
            limit 20
        """

        private const val CHAPTER_CATALOG_SQL = """
            select
                c.id::text as id,
                c.chapter_order,
                ct.title,
                c.updated_at
            from chapter c
            join chapter_translation ct on ct.chapter_id = c.id
            where c.book_id = ?::uuid
              and c.publication_status = 'published'
              and ct.publication_status = 'published'
            order by c.chapter_order
        """

        private const val CHAPTER_CONTENT_SQL = """
            select
                c.id::text as id,
                c.book_id::text as book_id,
                ct.title,
                ct.language,
                ct.paragraphs
            from chapter c
            join chapter_translation ct on ct.chapter_id = c.id
            where ct.language = ?
              and c.id = ?::uuid
              and c.publication_status = 'published'
              and ct.publication_status = 'published'
        """
    }
}

object JdbcContentRowMapper {
    private val objectMapper = jacksonObjectMapper()

    fun category(row: Map<String, Any?>): Category = Category(
        id = row.requiredString("id"),
        name = row.requiredString("name"),
        slug = row.requiredString("slug"),
    )

    fun bookSummary(row: Map<String, Any?>): BookSummary = BookSummary(
        id = row.requiredString("id"),
        title = row.requiredString("title"),
        author = row.requiredString("author"),
        coverUrl = row.optionalString("cover_url"),
        description = row.optionalString("description"),
        status = row.requiredString("serialization_status"),
        latestChapterTitle = row.optionalString("latest_chapter_title"),
        updatedAt = row.timestampString("updated_at"),
    )

    fun chapterSummary(row: Map<String, Any?>): ChapterSummary = ChapterSummary(
        id = row.requiredString("id"),
        order = row.requiredInt("chapter_order"),
        title = row.requiredString("title"),
        updatedAt = row.timestampString("updated_at"),
    )

    fun bookDetail(
        book: BookSummary,
        categories: List<Category>,
        chapters: List<ChapterSummary>,
    ): BookDetail = BookDetail(
        id = book.id,
        title = book.title,
        author = book.author,
        coverUrl = book.coverUrl,
        description = book.description,
        status = book.status,
        categories = categories,
        chapterCount = chapters.size,
        latestChapter = chapters.maxByOrNull { it.order },
    )

    fun chapterContent(row: Map<String, Any?>): ChapterContent = ChapterContent(
        id = row.requiredString("id"),
        bookId = row.requiredString("book_id"),
        title = row.requiredString("title"),
        language = row.requiredString("language"),
        paragraphs = parseParagraphs(row["paragraphs"]),
    )

    private fun parseParagraphs(value: Any?): List<String> {
        if (value == null) {
            return emptyList()
        }
        val raw = value.toString()
        if (raw.isBlank()) {
            return emptyList()
        }
        return objectMapper.readValue(raw)
    }

    private fun Map<String, Any?>.requiredString(key: String): String =
        requireNotNull(this[key]) { "Missing column $key" }.toString()

    private fun Map<String, Any?>.optionalString(key: String): String = this[key]?.toString().orEmpty()

    private fun Map<String, Any?>.requiredInt(key: String): Int {
        val value = requireNotNull(this[key]) { "Missing column $key" }
        return when (value) {
            is Number -> value.toInt()
            else -> value.toString().toInt()
        }
    }

    private fun Map<String, Any?>.timestampString(key: String): String {
        val value = this[key] ?: return ""
        return when (value) {
            is OffsetDateTime -> value.toString()
            else -> value.toString()
        }
    }
}
