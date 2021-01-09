package com.well.serverModels

import kotlinx.serialization.Serializable
import com.well.serverModels.serializers.DateSerializer

@Serializable(with = DateSerializer::class)
actual data class Date actual constructor(actual val millis: Long) {
    constructor(date: java.util.Date): this(date.time)
    actual constructor() : this(java.util.Date())
}