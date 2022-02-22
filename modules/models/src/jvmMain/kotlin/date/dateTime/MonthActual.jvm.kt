package com.well.modules.models.date.dateTime

import kotlinx.datetime.Month
import java.text.DateFormatSymbols

actual val Month.localizedName: String
    get() = DateFormatSymbols.getInstance().months[ordinal]

actual val Month.localizedShortName: String
    get() = DateFormatSymbols.getInstance().shortMonths[ordinal]