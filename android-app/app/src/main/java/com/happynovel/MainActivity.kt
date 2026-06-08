package com.happynovel

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.graphics.drawable.GradientDrawable
import com.happynovel.reader.NavigationTab
import com.happynovel.reader.FileReaderStateStore
import com.happynovel.reader.PersistedReaderLocalRepository
import com.happynovel.reader.ReaderLaunchTextModel
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
            setPadding(dp(20), dp(28), dp(20), dp(28))
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        content.addView(header(textModel))
        content.addView(filterSummary(textModel.bookListFilterLabel))
        textModel.sections.filter { it.body.isNotBlank() }.forEach { section ->
            content.addView(sectionCard(section.title, section.body))
        }
        content.addView(bottomTabs(textModel.bottomTabs))

        return ScrollView(this).apply {
            addView(content)
        }
    }

    private fun header(textModel: ReaderLaunchTextModel): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(12))
        addView(TextView(this@MainActivity).apply {
            text = textModel.title
            textSize = 30f
            setTextColor(Color.rgb(22, 28, 36))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.START
        })
        addView(TextView(this@MainActivity).apply {
            text = "Discover translated web novels and continue reading."
            textSize = 15f
            setTextColor(Color.rgb(91, 101, 116))
            setPadding(0, dp(6), 0, 0)
        })
    }

    private fun filterSummary(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(Color.rgb(48, 66, 88))
        setPadding(dp(12), dp(8), dp(12), dp(8))
        background = roundedBackground(Color.rgb(232, 239, 247), radius = dp(8))
    }

    private fun sectionCard(title: String, body: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = roundedBackground(Color.WHITE, radius = dp(8), strokeColor = Color.rgb(226, 231, 237))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(14)
        }
        addView(sectionTitle(title))
        addView(divider())
        addView(body(body))
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(Color.rgb(24, 31, 42))
        typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 0, 0, dp(8))
    }

    private fun body(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        setTextColor(Color.rgb(55, 65, 81))
        setLineSpacing(4f, 1f)
        setPadding(0, dp(10), 0, 0)
    }

    private fun bottomTabs(tabs: List<NavigationTab>): HorizontalScrollView = HorizontalScrollView(this).apply {
        isHorizontalScrollBarEnabled = false
        setPadding(0, dp(18), 0, 0)
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            tabs.forEachIndexed { index, tab ->
                addView(tabLabel(tab.label, isSelected = index == 0))
            }
        })
    }

    private fun tabLabel(text: String, isSelected: Boolean): TextView = TextView(this).apply {
        this.text = text
        textSize = 14f
        gravity = Gravity.START
        setTextColor(if (isSelected) Color.WHITE else Color.rgb(48, 66, 88))
        typeface = if (isSelected) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        setPadding(dp(14), dp(8), dp(14), dp(8))
        background = roundedBackground(
            color = if (isSelected) Color.rgb(40, 91, 145) else Color.rgb(232, 239, 247),
            radius = dp(8),
        )
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            rightMargin = dp(8)
        }
    }

    private fun divider(): View = View(this).apply {
        setBackgroundColor(Color.rgb(235, 238, 242))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(1),
        )
    }

    private fun roundedBackground(
        color: Int,
        radius: Int,
        strokeColor: Int? = null,
    ): GradientDrawable = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius.toFloat()
        strokeColor?.let { setStroke(dp(1), it) }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
