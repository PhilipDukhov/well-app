package com.well.modules.models

import kotlinx.serialization.Serializable
import com.well.modules.models.serializers.DateSerializer
import io.ktor.util.date.*

@Serializable(with = DateSerializer::class)
expect class Date {
    constructor()
    constructor(millis: Long)
    constructor(seconds: Double)

    val millis: Long
    val millisSinceNow: Long
}

operator fun Date.compareTo(b: Date): Int =
    millis.compareTo(b.millis)

fun Date.secondsSinceNow(): Double =
    millisSinceNow.toTimeInterval()

fun Long.toTimeInterval() : Double =
    (this / 1000).toDouble()

fun Double.toMillis() : Long =
    (this * 1000).toLong()

fun Date.toGMTDate() = GMTDate(millis)
