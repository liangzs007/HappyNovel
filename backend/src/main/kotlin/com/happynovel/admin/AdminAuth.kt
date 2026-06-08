package com.happynovel.admin

import com.happynovel.audit.AuditLogService
import com.happynovel.audit.JdbcAuditLogService
import com.happynovel.audit.InMemoryAuditLogService
import com.happynovel.content.ContentDatabaseClient
import com.happynovel.content.JdbcTemplateContentDatabaseClient
import com.happynovel.content.MissingContentDatabaseClient
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

data class AdminLoginRequest(
    val username: String,
    val password: String,
)

data class AdminLoginResponse(
    val username: String,
    val token: String,
)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidAdminCredentialsException : RuntimeException("Invalid admin credentials")

class AdminAuthService(
    private val username: String,
    private val password: String,
    private val auditLogService: AuditLogService,
) {
    fun login(request: AdminLoginRequest): AdminLoginResponse {
        if (request.username != username || request.password != password) {
            throw InvalidAdminCredentialsException()
        }

        auditLogService.record(
            actor = request.username,
            action = "ADMIN_LOGIN",
            targetType = "admin_user",
            targetId = request.username,
            summary = "管理员登录",
        )

        return AdminLoginResponse(
            username = request.username,
            token = "admin-session-${UUID.randomUUID()}",
        )
    }
}

@RestController
@RequestMapping("/api/admin/auth")
class AdminAuthController(
    private val authService: AdminAuthService,
) {
    @PostMapping("/login")
    fun login(@RequestBody request: AdminLoginRequest): AdminLoginResponse = authService.login(request)
}

@Configuration
class AdminAuthConfiguration(
    private val environment: Environment,
    private val jdbcTemplateProvider: ObjectProvider<JdbcTemplate>,
) {
    @Bean
    fun auditLogService(): AuditLogService =
        when (environment.getProperty("app.audit.repository-mode", "SEED").uppercase()) {
            "JDBC" -> JdbcAuditLogService(databaseClient())
            else -> InMemoryAuditLogService()
        }

    @Bean
    fun adminAuthService(
        @Value("\${app.admin.username:admin}") username: String,
        @Value("\${app.admin.password:change-me}") password: String,
        auditLogService: AuditLogService,
    ): AdminAuthService = AdminAuthService(username, password, auditLogService)

    private fun databaseClient(): ContentDatabaseClient =
        jdbcTemplateProvider.ifAvailable
            ?.let(::JdbcTemplateContentDatabaseClient)
            ?: MissingContentDatabaseClient
}
