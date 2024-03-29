package com.well.modules.utils.viewUtils

import java.util.*

actual fun timeZonesIdentifiersList(): Set<String> =
    TimeZone.getAvailableIDs().toSet()

actual fun currentTimeZoneIdentifier(): String =
    TimeZone.getDefault().id