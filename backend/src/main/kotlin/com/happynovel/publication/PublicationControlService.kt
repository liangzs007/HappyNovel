package com.happynovel.publication

import com.happynovel.admin.stringValue
import com.happynovel.content.ContentDatabaseClient
import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

interface PublicationControlService {
    fun isBookPublished(bookId: String): Boolean

    fun isChapterPublished(chapterId: String): Boolean

    fun unpublishBook(bookId: String)

    fun hideChapter(chapterId: String)
}

class InMemoryPublicationControlService : PublicationControlService {
    private val unpublishedBookIds = mutableSetOf<String>()
    private val hiddenChapterIds = mutableSetOf<String>()

    override fun isBookPublished(bookId: String): Boolean = bookId !in unpublishedBookIds

    override fun isChapterPublished(chapterId: String): Boolean = chapterId !in hiddenChapterIds

    override fun unpublishBook(bookId: String) {
        unpublishedBookIds += bookId
    }

    override fun hideChapter(chapterId: String) {
        hiddenChapterIds += chapterId
    }
}

class JdbcPublicationControlService(
    private val databaseClient: ContentDatabaseClient,
) : PublicationControlService {
    override fun isBookPublished(bookId: String): Boolean =
        publicationStatus(
            """
                select publication_status
                from book
                where id = ?
                limit 1
            """.trimIndent(),
            bookId,
        ) == "published"

    override fun isChapterPublished(chapterId: String): Boolean =
        publicationStatus(
            """
                select publication_status
                from chapter
                where id = ?
                limit 1
            """.trimIndent(),
            chapterId,
        ) == "published"

    override fun unpublishBook(bookId: String) {
        databaseClient.update(
            """
                update book
                set publication_status = ?, updated_at = now()
                where id = ?
            """.trimIndent(),
            "unpublished",
            UUID.fromString(bookId),
        )
    }

    override fun hideChapter(chapterId: String) {
        databaseClient.update(
            """
                update chapter
                set publication_status = ?, updated_at = now()
                where id = ?
            """.trimIndent(),
            "hidden",
            UUID.fromString(chapterId),
        )
    }

    private fun publicationStatus(sql: String, id: String): String =
        databaseClient.query(sql, UUID.fromString(id))
            .firstOrNull()
            ?.stringValue("publication_status")
            ?: "unpublished"
}

@Configuration
class PublicationControlConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun publicationControlService(): PublicationControlService =
        when (environment.getProperty("app.publication.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcPublicationControlService(databaseClient())
            else -> InMemoryPublicationControlService()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
