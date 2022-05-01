package com.well.server

import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*

enum class AuthName {
    Twitter,
    Apple,
    Main,
    Admin,
}

fun Route.authenticate(
    vararg authConfigurations: AuthName = arrayOf(),
    optional: Boolean = false,
    build: Route.() -> Unit,
) = authenticate(
    optional = optional,
    build = build,
    configurations = authConfigurations.map { it.name }.toTypedArray()
)

fun AuthenticationConfig.oauth(
    name: AuthName? = null,
    configure: OAuthAuthenticationProvider.Config.() -> Unit
) = oauth(name?.name, configure)

fun AuthenticationConfig.jwt(
    name: AuthName? = null,
    configure: JWTAuthenticationProvider.Config.() -> Unit
) = jwt(name?.name, configure)

fun AuthenticationConfig.basic(
    name: AuthName? = null,
    configure: BasicAuthenticationProvider.Config.() -> Unit
) = basic(name?.name, configure)
