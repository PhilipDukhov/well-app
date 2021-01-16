package com.well.serverModels

import com.well.serverModels.serializers.DateSerializer
import kotlinx.serialization.Serializable
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

@Serializable(with = DateSerializer::class)
actual data class Date(val date: NSDate) {
    actual constructor(millis: Long) : this(NSDate.dateWithTimeIntervalSince1970(millis.toDouble()))
    actual constructor() : this(NSDate())

    actual val millis = (date.timeIntervalSince1970 * 1000).toLong()

    override fun toString(): String = "wtf ${dateFormatter.stringFromDate(date)}"

    companion object {
        val dateFormatter = NSDateFormatter().apply {
            dateFormat = "y-MM-dd H:m:ss.SSSS"
        }
    }
}