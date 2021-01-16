package com.well.serverModels

import kotlinx.serialization.Serializable
import com.well.serverModels.serializers.DateSerializer

@Serializable(with = DateSerializer::class)
expect class Date {
    constructor()
    constructor(millis: Long)

    val millis: Long
}

operator fun Date.compareTo(b: Date): Int =
    millis.compareTo(b.millis)
