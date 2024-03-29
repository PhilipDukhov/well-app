package com.well.modules.models.date

import com.well.modules.models.serializers.DateSerializer
import kotlinx.serialization.Serializable
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeIntervalSinceNow

@Serializable(with = DateSerializer::class)
actual data class Date(val date: NSDate) {
    actual constructor(millis: Long) : this(NSDate.dateWithTimeIntervalSince1970(millis.toTimeInterval()))
    actual constructor(seconds: Double) : this(NSDate.dateWithTimeIntervalSince1970(seconds))
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