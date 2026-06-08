package com.happynovel.reader

data class ReaderLaunchTextModel(
    val title: String,
    val sections: List<ReaderTextSection>,
)

data class ReaderTextSection(
    val title: String,
    val body: String,
)

object ReaderLaunchTextModelFactory {
    fun create(
        loader: ReaderScreenLoader,
        bookId: String = "book-seed-1",
        chapterId: String = "chapter-seed-1",
    ): ReaderLaunchTextModel {
        val home = loader.home()
        val categories = loader.categories()
        val books = loader.books(category = "fantasy", status = "ongoing", sort = "popular", limit = 12)
        val detail = loader.bookDetail(bookId)
        val catalog = loader.chapterCatalog(bookId)
        val reader = loader.reader(bookId, chapterId)
        val homeContent = home.content

        return ReaderLaunchTextModel(
            title = homeContent?.title ?: "HappyNovel",
            sections = buildList {
                if (homeContent == null) {
                    add(ReaderTextSection("Home", home.message))
                } else {
                    homeContent.sections.forEach { section ->
                        add(
                            ReaderTextSection(
                                title = section.title,
                                body = section.books.joinToString("\n\n") {
                                    "${it.title}\n${it.author}\n${it.latestChapterTitle}"
                                },
                            ),
                        )
                    }
                }

                add(
                    ReaderTextSection(
                        title = categories.content?.title ?: "Categories",
                        body = categories.content?.categories?.joinToString { it.name } ?: categories.message,
                    ),
                )
                add(
                    ReaderTextSection(
                        title = books.content?.title ?: "Book List",
                        body = books.content?.books?.joinToString("\n\n") {
                            "${it.title}\n${it.author}\n${it.latestChapterTitle}"
                        } ?: books.message,
                    ),
                )
                add(
                    ReaderTextSection(
                        title = "Book Detail",
                        body = detail.content?.let { "${it.title}\n${it.primaryAction}\n${it.chapterCountLabel}" } ?: detail.message,
                    ),
                )
                add(
                    ReaderTextSection(
                        title = catalog.content?.title ?: "Chapters",
                        body = catalog.content?.chapters?.joinToString("\n") { "${it.order}. ${it.title}" } ?: catalog.message,
                    ),
                )
                add(
                    ReaderTextSection(
                        title = "Reader Preview",
                        body = reader.content?.let { listOf(it.title).plus(it.paragraphs).joinToString("\n\n") } ?: reader.message,
                    ),
                )
                add(
                    ReaderTextSection(
                        title = homeContent?.bottomTabs?.joinToString("   ") { it.label } ?: "Home   Categories   Bookshelf",
                        body = "",
                    ),
                )
            },
        )
    }
}
