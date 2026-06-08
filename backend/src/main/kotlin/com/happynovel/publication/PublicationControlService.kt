package com.happynovel.publication

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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

@Configuration
class PublicationControlConfiguration {
    @Bean
    fun publicationControlService(): PublicationControlService = InMemoryPublicationControlService()
}
