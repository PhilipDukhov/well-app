package com.well.modules.models.date.dateTime

import kotlinx.datetime.Month
import platform.Foundation.NSCalendar

actual val Month.localizedShortName
    get() = NSCalendar.currentCalendar.shortStandaloneMonthSymbols[ordinal] as String

actual val Month.localizedName
    get() = (NSCalendar.currentCalendar.standaloneMonthSymbols[ordinal] as String)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }