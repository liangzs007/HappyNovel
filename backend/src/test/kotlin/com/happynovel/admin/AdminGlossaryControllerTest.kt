package com.happynovel.admin

import com.happynovel.translation.AddGlossaryTermRequest
import com.happynovel.translation.GlossaryTermType
import com.happynovel.translation.InMemoryGlossaryService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AdminGlossaryControllerTest {
    @Test
    fun `admin glossary endpoint returns term rows`() {
        val glossary = InMemoryGlossaryService()
        glossary.addTerm(
            AddGlossaryTermRequest(
                bookId = "book-seed-1",
                sourceTerm = "青云宗",
                translatedTerm = "Azure Cloud Sect",
                type = GlossaryTermType.ORGANIZATION,
                description = "主角初入的宗门",
            ),
        )
        val controller = AdminGlossaryController(glossary)

        val response = controller.terms(bookId = "book-seed-1")

        assertEquals("暂无术语，请为书籍添加术语表。", response.emptyText)
        assertEquals(1, response.terms.size)
        assertEquals("青云宗", response.terms.single().sourceTerm)
        assertEquals("Azure Cloud Sect", response.terms.single().translatedTerm)
        assertEquals("ORGANIZATION", response.terms.single().type)
        assertEquals("启用", response.terms.single().enabledStatus)
    }

    @Test
    fun `admin glossary endpoint creates term rows`() {
        val glossary = InMemoryGlossaryService()
        val controller = AdminGlossaryController(glossary)

        val response = controller.createTerm(
            AddGlossaryTermRequest(
                bookId = "book-seed-1",
                sourceTerm = "林辰",
                translatedTerm = "Lin Chen",
                type = GlossaryTermType.PERSON,
                description = "主角姓名",
            ),
        )

        assertEquals(1, response.terms.size)
        assertEquals("林辰", response.terms.single().sourceTerm)
        assertEquals("Lin Chen", response.terms.single().translatedTerm)
        assertEquals("PERSON", response.terms.single().type)
    }
}
