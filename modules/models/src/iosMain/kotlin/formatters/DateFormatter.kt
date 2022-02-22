package com.well.modules.models.formatters

import com.well.modules.models.date.Date
import platform.Foundation.*

@ThreadLocal
private val timeZoneFormatter = NSDateFormatter().apply {
    dateFormat = "O"
}

@ThreadLocal
private val timeFormatter = NSDateFormatter().apply {
    dateStyle = NSDateFormatterNoStyle
    timeStyle = NSDateFormatterShortStyle
}

actual fun DateFormatter.Companion.format(date: Date, timeZoneIdentifier: String): String {
    timeZoneFormatter.timeZone = NSTimeZone.timeZoneWithName(timeZoneIdentifier) ?: NSTimeZone.defaultTimeZone
    timeFormatter.timeZone = timeZoneFormatter.timeZone
    return "${timeZoneFormatter.stringFromDate(date.date)} ${timeFormatter.stringFromDate(date.date)}"
}

actual fun DateFormatter.Companion.formatChatMessage(date: Date): String {
    timeFormatter.timeZone = NSTimeZone.defaultTimeZone
    return timeFormatter.stringFromDate(date.date)
}