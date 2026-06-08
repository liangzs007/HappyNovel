package com.happynovel.content

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

class MigrationDefinitionTest {
    @Test
    fun `initial migration defines MVP core tables`() {
        val migration = Files.readString(Path.of("src/main/resources/db/migration/V1__initial_schema.sql"))

        val expectedTables = listOf(
            "site_config",
            "book",
            "book_source",
            "chapter",
            "chapter_raw_content",
            "chapter_clean_content",
            "chapter_translation",
            "taxonomy_category",
            "taxonomy_tag",
            "book_tag",
            "glossary_term",
            "pending_glossary_term",
            "pipeline_task",
            "admin_user",
            "audit_log",
            "anonymous_device",
            "reading_event",
            "ad_config",
            "compliance_config",
            "copyright_complaint",
        )

        expectedTables.forEach { table ->
            assertTrue(migration.contains("create table if not exists $table"), "Missing table $table")
        }
    }

    @Test
    fun `second migration defines book category relationship`() {
        val migration = Files.readString(Path.of("src/main/resources/db/migration/V2__book_category.sql"))

        assertTrue(migration.contains("create table if not exists book_category"))
        assertTrue(migration.contains("create index if not exists idx_book_category_category"))
    }
}
