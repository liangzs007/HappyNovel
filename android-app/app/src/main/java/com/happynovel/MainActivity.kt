package com.happynovel

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.happynovel.reader.ReaderLaunchStateFactory

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    private fun buildContent(): ScrollView {
        val launchState = ReaderLaunchStateFactory.create()

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        content.addView(title(launchState.home.title))
        launchState.home.sections.forEach { section ->
            content.addView(sectionTitle(section.title))
            section.books.forEach { content.addView(body("${it.title}\n${it.author}\n${it.latestChapterTitle}")) }
        }
        content.addView(sectionTitle(launchState.categories.title))
        content.addView(body(launchState.categories.categories.joinToString { it.name }))
        content.addView(sectionTitle("Book Detail"))
        content.addView(body("${launchState.bookDetail.title}\n${launchState.bookDetail.primaryAction}\n${launchState.bookDetail.chapterCountLabel}"))
        content.addView(sectionTitle(launchState.chapterCatalog.title))
        launchState.chapterCatalog.chapters.forEach { content.addView(body("${it.order}. ${it.title}")) }
        content.addView(sectionTitle("Reader Preview"))
        content.addView(body(launchState.reader.title))
        launchState.reader.paragraphs.forEach { content.addView(body(it)) }
        content.addView(sectionTitle(launchState.home.bottomTabs.joinToString("   ") { it.label }))

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
