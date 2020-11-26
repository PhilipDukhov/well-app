package com.well.server.utils

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*

val ApplicationCall.authUserId: Int
    get() = principal<JWTPrincipal>()!!.payload.claims["id"]!!.asInt()