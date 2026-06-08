package com.happynovel

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.happynovel.reader.FileReaderStateStore
import com.happynovel.reader.PersistedReaderLocalRepository
import com.happynovel.reader.ReaderAppCoordinator
import com.happynovel.reader.ReaderLaunchTextModelFactory
import com.happynovel.reader.ReaderRemoteDataSourceFactory
import com.happynovel.reader.ReaderScreenLoader
import java.io.File

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildContent())
    }

    private fun buildContent(): ScrollView {
        val remoteDataSource = ReaderRemoteDataSourceFactory.create(BuildConfig.HAPPYNOVEL_API_BASE_URL)
        val localRepository = PersistedReaderLocalRepository(
            FileReaderStateStore(File(filesDir, "reader-state.json")),
        )
        val loader = ReaderScreenLoader(
            ReaderAppCoordinator(remoteDataSource, localRepository),
        )
        val textModel = ReaderLaunchTextModelFactory.create(loader)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 48, 40, 48)
        }

        content.addView(title(textModel.title))
        textModel.sections.forEach { section ->
            content.addView(sectionTitle(section.title))
            if (section.body.isNotBlank()) {
                content.addView(body(section.body))
            }
        }

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
