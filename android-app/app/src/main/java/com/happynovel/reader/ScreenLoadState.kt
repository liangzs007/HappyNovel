package com.happynovel.reader

data class ScreenLoadState<T>(
    val content: T?,
    val message: String,
    val isLoading: Boolean,
    val shouldShowRetry: Boolean,
) {
    companion object {
        fun <T> loading(): ScreenLoadState<T> = ScreenLoadState(
            content = null,
            message = "Loading...",
            isLoading = true,
            shouldShowRetry = false,
        )

        fun <T> error(message: String): ScreenLoadState<T> = ScreenLoadState(
            content = null,
            message = message,
            isLoading = false,
            shouldShowRetry = true,
        )

        fun <T> empty(message: String): ScreenLoadState<T> = ScreenLoadState(
            content = null,
            message = message,
            isLoading = false,
            shouldShowRetry = false,
        )

        fun <T> content(content: T): ScreenLoadState<T> = ScreenLoadState(
            content = content,
            message = "",
            isLoading = false,
            shouldShowRetry = false,
        )
    }
}
