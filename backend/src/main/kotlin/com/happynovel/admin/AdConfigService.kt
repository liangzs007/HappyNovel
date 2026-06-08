package com.happynovel.admin

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

data class AdminAdConfigResponse(
    val enabled: Boolean,
    val readerBannerEnabled: Boolean,
    val interstitialEveryChapters: Int,
)

data class UpdateAdConfigRequest(
    val enabled: Boolean,
    val readerBannerEnabled: Boolean,
    val interstitialEveryChapters: Int,
)

interface AdConfigService {
    fun current(): AdminAdConfigResponse

    fun update(request: UpdateAdConfigRequest): AdminAdConfigResponse
}

class InMemoryAdConfigService : AdConfigService {
    private var config = AdminAdConfigResponse(
        enabled = true,
        readerBannerEnabled = true,
        interstitialEveryChapters = 5,
    )

    override fun current(): AdminAdConfigResponse = config

    override fun update(request: UpdateAdConfigRequest): AdminAdConfigResponse {
        config = AdminAdConfigResponse(
            enabled = request.enabled,
            readerBannerEnabled = request.readerBannerEnabled,
            interstitialEveryChapters = request.interstitialEveryChapters.coerceAtLeast(0),
        )
        return config
    }
}

@Configuration
class AdConfiguration {
    @Bean
    fun adConfigService(): AdConfigService = InMemoryAdConfigService()
}
