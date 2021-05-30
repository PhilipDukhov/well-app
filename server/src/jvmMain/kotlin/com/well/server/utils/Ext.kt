package com.well.server.utils

import io.ktor.application.*
import io.ktor.http.*

fun ApplicationEnvironment.configProperty(path: String): String =
    config.property(path)
        .getString()

fun ParametersBuilder.append(vararg map: Pair<String, Any>) =
    map.forEach {
        append(it.first, it.second.toString())
    }

fun Boolean.toLong(): Long = if (this) 1 else 0