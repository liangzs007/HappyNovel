package com.happynovel.reading

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class ReadingEvent(
    val deviceId: String,
    val bookId: String,
    val chapterId: String,
    val percent: Float,
)

interface ReadingEventService {
    fun record(event: ReadingEvent): ReadingEvent

    fun events(): List<ReadingEvent>
}

class InMemoryReadingEventService : ReadingEventService {
    private val events = mutableListOf<ReadingEvent>()

    override fun record(event: ReadingEvent): ReadingEvent {
        val normalized = event.copy(percent = event.percent.coerceIn(0f, 1f))
        events += normalized
        return normalized
    }

    override fun events(): List<ReadingEvent> = events.toList()
}

@Configuration
class ReadingEventConfiguration {
    @Bean
    fun readingEventService(): ReadingEventService = InMemoryReadingEventService()
}
