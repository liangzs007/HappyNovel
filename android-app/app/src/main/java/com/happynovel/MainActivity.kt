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
import com.happynovel.reader.BookDetailUiState
import com.happynovel.reader.BookSummary
import com.happynovel.reader.BookshelfUiState
import com.happynovel.reader.ChapterCatalogUiState
import com.happynovel.reader.ChapterRowUiState
import com.happynovel.reader.FileReaderStateStore
import com.happynovel.reader.HomeUiState
import com.happynovel.reader.ReaderScreenRoute
import com.happynovel.reader.ReaderNavigation
import com.happynovel.reader.ReaderNavigationState
import com.happynovel.reader.PersistedReaderLocalRepository
import com.happynovel.reader.ReaderAppCoordinator
import com.happynovel.reader.ReaderRemoteDataSourceFactory
import com.happynovel.reader.ReaderScreenLoader
import com.happynovel.reader.ReaderUiState
import com.happynovel.reader.ScreenLoadState
import com.happynovel.reader.ReaderTheme
import java.io.File

class MainActivity : Activity() {
    private lateinit var loader: ReaderScreenLoader
    private lateinit var coordinator: ReaderAppCoordinator
    private val navigationState = ReaderNavigationState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remoteDataSource = ReaderRemoteDataSourceFactory.create(BuildConfig.HAPPYNOVEL_API_BASE_URL)
        val localRepository = PersistedReaderLocalRepository(
            FileReaderStateStore(File(filesDir, "reader-state.json")),
        )
        coordinator = ReaderAppCoordinator(remoteDataSource, localRepository)
        loader = ReaderScreenLoader(coordinator)
        render()
    }

    private fun render() {
        setContentView(buildContent())
    }

    private fun buildContent(): ScrollView {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
            setBackgroundColor(Color.rgb(247, 248, 250))
        }

        when (val route = navigationState.currentRoute) {
            ReaderScreenRoute.Home -> content.renderHome()
            ReaderScreenRoute.Categories -> content.renderCategories()
            ReaderScreenRoute.Bookshelf -> content.renderBookshelf()
            is ReaderScreenRoute.BookDetail -> content.renderBookDetail(route.bookId)
            is ReaderScreenRoute.ChapterCatalog -> content.renderChapterCatalog(route.bookId)
            is ReaderScreenRoute.Reader -> content.renderReader(route.bookId, route.chapterId)
        }

        return ScrollView(this).apply {
            addView(content)
        }
    }

    private fun LinearLayout.renderHome() {
        addView(header("HappyNovel", "Discover translated web novels and continue reading."))
        renderLoadState(
            state = loader.home(),
            onContent = { home ->
                home.sections.forEach { section ->
                    addView(sectionCard(section.title) {
                        section.books.forEach { book ->
                            addView(bookRow(book))
                        }
                    })
                }
            },
        )
        addView(bottomTabs("home"))
    }

    private fun LinearLayout.renderCategories() {
        addView(header("Categories", "Browse by genre and status."))
        addView(filterSummary("Fantasy / ongoing / popular"))
        renderLoadState(
            state = loader.books(category = "fantasy", status = "ongoing", sort = "popular", limit = 12),
            onContent = { list ->
                addView(sectionCard(list.title) {
                    list.books.forEach { book ->
                        addView(bookRow(book))
                    }
                })
            },
        )
        addView(bottomTabs("categories"))
    }

    private fun LinearLayout.renderBookshelf() {
        addView(header("Bookshelf", "Saved books and reading progress."))
        renderLoadState(
            state = loader.bookshelf(),
            onContent = { bookshelf ->
                addView(sectionCard(bookshelf.title) {
                bookshelf.books.forEach { book ->
                    addView(bookshelfBookRow(bookshelf, book))
                }
                })
            },
        )
        addView(bottomTabs("bookshelf"))
    }

    private fun LinearLayout.renderBookDetail(bookId: String) {
        renderBackButton()
        renderLoadState(
            state = loader.bookDetail(bookId),
            onContent = { detail ->
                addView(header(detail.title, detail.author))
                addView(sectionCard("About") {
                    addView(body(detail.description))
                    addView(body("${detail.status} · ${detail.chapterCountLabel}"))
                    addView(actionButton(detail.primaryAction) {
                        val chapterId = detail.latestChapterId
                        if (chapterId != null) {
                            coordinator.startReading(bookId, chapterId)
                            navigationState.openReader(bookId, chapterId)
                            render()
                        }
                    })
                    addView(actionButton(detail.bookshelfAction) {
                        coordinator.saveBookToBookshelf(bookId)
                        render()
                    })
                    addView(actionButton("Chapters") {
                        navigationState.openChapters(bookId)
                        render()
                    })
                })
            },
        )
    }

    private fun LinearLayout.renderChapterCatalog(bookId: String) {
        renderBackButton()
        renderLoadState(
            state = loader.chapterCatalog(bookId),
            onContent = { catalog ->
                addView(header(catalog.title, "Select a chapter to read."))
                addView(sectionCard("Chapters") {
                    catalog.chapters.forEach { chapter ->
                        addView(chapterRow(bookId, chapter))
                    }
                })
            },
        )
    }

    private fun LinearLayout.renderReader(bookId: String, chapterId: String) {
        renderBackButton()
        renderLoadState(
            state = loader.reader(bookId, chapterId),
            onContent = { reader ->
                addView(header(reader.title, "${reader.fontSizeLabel} · ${reader.progressLabel} · ${reader.themeLabel()}"))
                addView(readerCard(reader) {
                    addView(actionButton("A-") {
                        coordinator.decreaseFontSize()
                        render()
                    })
                    addView(actionButton("A+") {
                        coordinator.increaseFontSize()
                        render()
                    })
                    addView(actionButton(reader.themeActionLabel()) {
                        coordinator.toggleTheme()
                        render()
                    })
                    reader.paragraphs.forEach { paragraph ->
                        addView(readerParagraph(paragraph, reader.fontSizeSp, reader.theme))
                    }
                    reader.readerAdLabel?.let { label ->
                        addView(adPlaceholder(label, reader.theme))
                    }
                    reader.adDisclosureText?.let { disclosure ->
                        addView(readerFootnote(disclosure, reader.theme))
                    }
                    addView(actionButton("Mark Chapter Read") {
                        coordinator.updateReadingProgress(bookId, chapterId, 1f)
                        render()
                    })
                })
            },
        )
    }

    private fun <T> LinearLayout.renderLoadState(
        state: ScreenLoadState<T>,
        onContent: LinearLayout.(T) -> Unit,
    ) {
        when {
            state.content != null -> onContent(state.content)
            else -> addView(sectionCard("Status") {
                addView(body(state.message))
            })
        }
    }

    private fun LinearLayout.renderBackButton() {
        addView(actionButton("Back") {
            navigationState.goBack()
            render()
        })
    }

    private fun header(title: String, subtitle: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(0, 0, 0, dp(12))
        addView(TextView(this@MainActivity).apply {
            text = title
            textSize = 30f
            setTextColor(Color.rgb(22, 28, 36))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.START
        })
        addView(TextView(this@MainActivity).apply {
            text = subtitle
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

    private fun sectionCard(title: String, contentBuilder: LinearLayout.() -> Unit): LinearLayout = LinearLayout(this).apply {
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
        contentBuilder()
    }

    private fun readerCard(reader: ReaderUiState, contentBuilder: LinearLayout.() -> Unit): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = roundedBackground(
            color = if (reader.theme == ReaderTheme.DARK) Color.rgb(28, 34, 44) else Color.WHITE,
            radius = dp(8),
            strokeColor = if (reader.theme == ReaderTheme.DARK) Color.rgb(62, 72, 88) else Color.rgb(226, 231, 237),
        )
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(14)
        }
        addView(sectionTitle("Reader", reader.theme))
        addView(divider())
        contentBuilder()
    }

    private fun sectionTitle(text: String, theme: ReaderTheme = ReaderTheme.LIGHT): TextView = TextView(this).apply {
        this.text = text
        textSize = 18f
        setTextColor(if (theme == ReaderTheme.DARK) Color.rgb(241, 245, 249) else Color.rgb(24, 31, 42))
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

    private fun readerParagraph(text: String, fontSizeSp: Int, theme: ReaderTheme): TextView = TextView(this).apply {
        this.text = text
        textSize = fontSizeSp.toFloat()
        setTextColor(if (theme == ReaderTheme.DARK) Color.rgb(230, 232, 236) else Color.rgb(55, 65, 81))
        setLineSpacing(6f, 1.15f)
        setPadding(0, dp(12), 0, 0)
    }

    private fun adPlaceholder(text: String, theme: ReaderTheme): TextView = TextView(this).apply {
        this.text = text
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(if (theme == ReaderTheme.DARK) Color.rgb(198, 208, 222) else Color.rgb(82, 97, 116))
        setPadding(dp(12), dp(12), dp(12), dp(12))
        background = roundedBackground(
            color = if (theme == ReaderTheme.DARK) Color.rgb(42, 51, 64) else Color.rgb(239, 243, 248),
            radius = dp(8),
            strokeColor = if (theme == ReaderTheme.DARK) Color.rgb(82, 94, 112) else Color.rgb(216, 224, 233),
        )
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(16)
        }
    }

    private fun readerFootnote(text: String, theme: ReaderTheme): TextView = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(if (theme == ReaderTheme.DARK) Color.rgb(163, 174, 190) else Color.rgb(91, 101, 116))
        setPadding(0, dp(8), 0, 0)
    }

    private fun bookRow(book: BookSummary): TextView = TextView(this).apply {
        text = "${book.title}\n${book.author}\n${book.latestChapterTitle}"
        textSize = 15f
        setTextColor(Color.rgb(55, 65, 81))
        setLineSpacing(4f, 1f)
        setPadding(0, dp(12), 0, dp(12))
        setOnClickListener {
            navigationState.openBook(book.id)
            render()
        }
    }

    private fun bookshelfBookRow(bookshelf: BookshelfUiState, book: BookSummary): TextView = TextView(this).apply {
        val progress = bookshelf.progressFor(book.id)
        text = buildString {
            appendLine(book.title)
            appendLine(book.author)
            append(book.latestChapterTitle)
            if (progress != null) {
                append("\nProgress ${progress.progressLabel}")
            }
        }
        textSize = 15f
        setTextColor(Color.rgb(55, 65, 81))
        setLineSpacing(4f, 1f)
        setPadding(0, dp(12), 0, dp(12))
        setOnClickListener {
            if (progress == null) {
                navigationState.openBook(book.id)
            } else {
                navigationState.openReader(book.id, progress.chapterId)
            }
            render()
        }
    }

    private fun chapterRow(bookId: String, chapter: ChapterRowUiState): TextView = TextView(this).apply {
        text = "${chapter.order}. ${chapter.title}"
        textSize = 15f
        setTextColor(Color.rgb(55, 65, 81))
        setPadding(0, dp(12), 0, dp(12))
        setOnClickListener {
            navigationState.openReader(bookId, chapter.id)
            render()
        }
    }

    private fun actionButton(text: String, onClick: () -> Unit): TextView = TextView(this).apply {
        this.text = text
        textSize = 15f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        setPadding(dp(14), dp(10), dp(14), dp(10))
        background = roundedBackground(Color.rgb(40, 91, 145), radius = dp(8))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = dp(12)
            rightMargin = dp(8)
        }
        setOnClickListener { onClick() }
    }

    private fun bottomTabs(activeKey: String): HorizontalScrollView = HorizontalScrollView(this).apply {
        isHorizontalScrollBarEnabled = false
        setPadding(0, dp(18), 0, 0)
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.HORIZONTAL
            ReaderNavigation.primaryTabs.forEach { tab ->
                addView(tabLabel(tab.label, isSelected = tab.key == activeKey) {
                    when (tab.key) {
                        "home" -> navigationState.openHome()
                        "categories" -> navigationState.openCategories()
                        "bookshelf" -> navigationState.openBookshelf()
                    }
                    render()
                })
            }
        })
    }

    private fun tabLabel(text: String, isSelected: Boolean, onClick: () -> Unit): TextView = TextView(this).apply {
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
        setOnClickListener { onClick() }
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

    private fun ReaderUiState.themeLabel(): String = if (theme == ReaderTheme.DARK) "Dark" else "Light"

    private fun ReaderUiState.themeActionLabel(): String = if (theme == ReaderTheme.DARK) "Light Mode" else "Dark Mode"
}
