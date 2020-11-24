package com.well.serverModels

import com.well.serverModels.serializers.DateSerializer
import kotlinx.serialization.Serializable
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

@Serializable(with = DateSerializer::class)
actual data class Date actual constructor(actual val millis: Long) {
    constructor(date: NSDate): this((date.timeIntervalSince1970 * 1000).toLong())
}

