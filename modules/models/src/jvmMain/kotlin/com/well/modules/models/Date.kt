package com.well.modules.models

import kotlinx.serialization.Serializable
import com.well.modules.models.serializers.DateSerializer

@Serializable(with = DateSerializer::class)
actual data class Date(val date: java.util.Date) {
    actual constructor(millis: Long) : this(java.util.Date(millis))
    actual constructor(seconds: Double) : this(java.util.Date((seconds * 1000).toLong()))
    actual constructor() : this(java.util.Date())

    actual val millis: Long = date.time

    actual val millisSinceNow: Long
        get() = java.util.Date().time - date.time

    override fun toString(): String = date.toString()
}