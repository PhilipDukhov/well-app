package com.well.serverModels

import kotlinx.serialization.Serializable
import com.well.serverModels.serializers.DateSerializer

@Serializable(with = DateSerializer::class)
actual data class Date(val date: java.util.Date) {
    actual constructor(millis: Long) : this(java.util.Date(millis))
    actual constructor() : this(java.util.Date())

    actual val millis: Long = date.time

    actual val millisSinceNow: Long
        get() = java.util.Date().time - date.time

    override fun toString(): String = date.toString()
}