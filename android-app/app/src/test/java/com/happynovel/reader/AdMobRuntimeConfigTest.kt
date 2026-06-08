package com.happynovel.reader

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdMobRuntimeConfigTest {
    @Test
    fun `runtime config disables banner when ad unit id is blank`() {
        val config = AdMobRuntimeConfig.from("")

        assertFalse(config.hasReaderBanner)
    }

    @Test
    fun `runtime config enables banner when ad unit id is configured`() {
        val config = AdMobRuntimeConfig.from("ca-app-pub-3940256099942544/6300978111")

        assertTrue(config.hasReaderBanner)
        assertEquals("ca-app-pub-3940256099942544/6300978111", config.readerBannerAdUnitId)
    }
}
