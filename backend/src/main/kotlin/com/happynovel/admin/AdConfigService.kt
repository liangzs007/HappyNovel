package com.happynovel.admin

import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient
import com.happynovel.content.ContentDatabaseClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.core.JdbcTemplate
import java.util.UUID

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

class JdbcAdConfigService(
    private val databaseClient: ContentDatabaseClient,
) : AdConfigService {
    override fun current(): AdminAdConfigResponse =
        databaseClient.query(
            """
                select enabled, reader_banner_enabled, interstitial_every_chapters
                from ad_config
                where scope_type = ? and scope_value = ?
                limit 1
            """.trimIndent(),
            GLOBAL_SCOPE_TYPE,
            GLOBAL_SCOPE_VALUE,
        ).firstOrNull()?.let(::mapAdConfig) ?: DEFAULT_CONFIG

    override fun update(request: UpdateAdConfigRequest): AdminAdConfigResponse {
        val config = AdminAdConfigResponse(
            enabled = request.enabled,
            readerBannerEnabled = request.readerBannerEnabled,
            interstitialEveryChapters = request.interstitialEveryChapters.coerceAtLeast(0),
        )
        databaseClient.update(
            """
                insert into ad_config(
                    id,
                    scope_type,
                    scope_value,
                    enabled,
                    reader_banner_enabled,
                    interstitial_every_chapters
                )
                values (?, ?, ?, ?, ?, ?)
                on conflict (id) do update set
                    enabled = excluded.enabled,
                    reader_banner_enabled = excluded.reader_banner_enabled,
                    interstitial_every_chapters = excluded.interstitial_every_chapters
            """.trimIndent(),
            GLOBAL_AD_CONFIG_ID,
            GLOBAL_SCOPE_TYPE,
            GLOBAL_SCOPE_VALUE,
            config.enabled,
            config.readerBannerEnabled,
            config.interstitialEveryChapters,
        )
        return config
    }

    private fun mapAdConfig(row: Map<String, Any?>): AdminAdConfigResponse = AdminAdConfigResponse(
        enabled = row.booleanValue("enabled"),
        readerBannerEnabled = row.booleanValue("reader_banner_enabled"),
        interstitialEveryChapters = row.intValue("interstitial_every_chapters"),
    )

    companion object {
        private val GLOBAL_AD_CONFIG_ID = UUID.fromString("00000000-0000-0000-0000-000000000101")
        private const val GLOBAL_SCOPE_TYPE = "global"
        private const val GLOBAL_SCOPE_VALUE = "app"
        private val DEFAULT_CONFIG = AdminAdConfigResponse(
            enabled = true,
            readerBannerEnabled = true,
            interstitialEveryChapters = 5,
        )
    }
}

@Configuration
class AdConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun adConfigService(): AdConfigService =
        when (environment.getProperty("app.admin.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcAdConfigService(databaseClient())
            else -> InMemoryAdConfigService()
        }

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}

internal fun Map<String, Any?>.booleanValue(key: String): Boolean = when (val value = this[key]) {
    is Boolean -> value
    is Number -> value.toInt() != 0
    is String -> value.equals("true", ignoreCase = true) || value == "1"
    else -> false
}

internal fun Map<String, Any?>.intValue(key: String): Int = when (val value = this[key]) {
    is Number -> value.toInt()
    is String -> value.toInt()
    else -> 0
}

internal fun Map<String, Any?>.stringValue(key: String): String = this[key]?.toString() ?: ""
