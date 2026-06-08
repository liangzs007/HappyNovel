package com.happynovel.reader

data class AdMobRuntimeConfig(
    val readerBannerAdUnitId: String,
) {
    val hasReaderBanner: Boolean = readerBannerAdUnitId.isNotBlank()

    companion object {
        fun from(readerBannerAdUnitId: String): AdMobRuntimeConfig = AdMobRuntimeConfig(
            readerBannerAdUnitId = readerBannerAdUnitId.trim(),
        )
    }
}
