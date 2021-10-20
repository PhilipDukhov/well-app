package com.well.modules.models.date

import kotlinx.serialization.Serializable
import com.well.modules.models.serializers.DateSerializer

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
