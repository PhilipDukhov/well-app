package com.well.server

import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.routing.*

enum class AuthName {
    Twitter,
    Apple,
    Main,
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

fun Authentication.Configuration.oauth(
    name: AuthName? = null,
    configure: OAuthAuthenticationProvider.Configuration.() -> Unit
) = oauth(name?.name, configure)

fun Authentication.Configuration.jwt(
    name: AuthName? = null,
    configure: JWTAuthenticationProvider.Configuration.() -> Unit
) = jwt(name?.name, configure)
