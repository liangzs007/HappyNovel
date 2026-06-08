package com.happynovel

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.happynovel.reader.BookSummary
import com.happynovel.reader.ChapterContent
import com.happynovel.reader.HomeState
import com.happynovel.reader.ReaderScreenState
import com.happynovel.reader.ReaderSettings
import com.happynovel.reader.ReaderUiStateFactory

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    private fun buildContent(): ScrollView {
        val book = BookSummary(
            id = "book-seed-1",
            title = "Dragon Gate",
            latestChapterTitle = "Chapter 2: The Trial",
            author = "Happy Novel Team",
            description = "A translated cultivation novel prepared for MVP API validation.",
            status = "ongoing",
        )
        val home = ReaderUiStateFactory.home(
            HomeState(
                appName = "HappyNovel",
                recommended = listOf(book),
                latestUpdates = listOf(book),
                popular = listOf(book),
                newBooks = listOf(book),
            ),
        )
        val reader = ReaderUiStateFactory.reader(
            ReaderScreenState(
                chapter = ChapterContent(
                    id = "chapter-seed-1",
                    title = "Chapter 1: Azure Cloud Sect",
                    paragraphs = listOf(
                        "The morning bell echoed across Azure Cloud Sect.",
                        "Lin Chen stepped through the Dragon Gate for the first time.",
                    ),
                ),
                settings = ReaderSettings.default(),
                progress = null,
            ),
        )

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        content.addView(title(home.title))
        home.sections.forEach { section ->
            content.addView(sectionTitle(section.title))
            section.books.forEach { content.addView(body("${it.title}\n${it.author}\n${it.latestChapterTitle}")) }
        }
        content.addView(sectionTitle("Reader Preview"))
        content.addView(body(reader.title))
        reader.paragraphs.forEach { content.addView(body(it)) }
        content.addView(sectionTitle(home.bottomTabs.joinToString("   ") { it.label }))

        return ScrollView(this).apply {
            addView(content)
        }
    }

    private fun title(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 28f
        gravity = Gravity.START
        setPadding(0, 0, 0, 24)
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        setPadding(0, 24, 0, 8)
    }

    private fun body(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 16f
        setPadding(0, 4, 0, 12)
    }
}
