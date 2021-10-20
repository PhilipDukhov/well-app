package com.well.modules.models.date.dateTime

import kotlinx.datetime.Month
import platform.Foundation.NSCalendar

actual val Month.localizedShortName
    get() = NSCalendar.currentCalendar.shortMonthSymbols[ordinal] as String

actual val Month.localizedName
    get() = NSCalendar.currentCalendar.monthSymbols[ordinal] as String