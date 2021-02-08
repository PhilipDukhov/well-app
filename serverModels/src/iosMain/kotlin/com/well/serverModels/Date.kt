package com.well.serverModels

import com.well.serverModels.serializers.DateSerializer
import kotlinx.serialization.Serializable
import platform.Foundation.*

@Serializable(with = DateSerializer::class)
actual data class Date(val date: NSDate) {
    actual constructor(millis: Long) : this(NSDate.dateWithTimeIntervalSince1970(millis.toTimeInterval()))
    actual constructor() : this(NSDate())

    actual val millis = date.timeIntervalSince1970.toMillis()

    actual val millisSinceNow: Long
        get() = -date.timeIntervalSinceNow.toMillis()

    override fun toString(): String = dateFormatter.stringFromDate(date)

    companion object {
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "y-MM-dd H:m:ss.SSSS"
        }
    }
}