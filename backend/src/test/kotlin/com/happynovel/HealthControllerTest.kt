package com.happynovel

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HealthControllerTest {
    @Test
    fun `health endpoint returns ok status`() {
        val response = HealthController().health()

        assertEquals("ok", response["status"])
    }
}
