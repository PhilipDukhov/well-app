package com.well.server.utils

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.util.*

fun ApplicationEnvironment.configProperty(path: String): String =
    config.property(path)
        .getString()

@OptIn(InternalAPI::class)
fun ParametersBuilder.append(vararg map: Pair<String, Any>) =
    map.forEach {
        append(it.first, it.second.toString())
    }