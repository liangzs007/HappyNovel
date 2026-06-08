package com.happynovel.content

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.assertEquals

class ContentConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(ContentConfiguration::class.java)
        .withPropertyValues("app.content.repository-mode=SEED")

    @Test
    fun `seed mode provides in memory content repository without jdbc bean`() {
        contextRunner.run { context ->
            val repository = context.getBean(ContentRepository::class.java)

            assertEquals("Dragon Gate", repository.homeBooks().single().title)
        }
    }
}
