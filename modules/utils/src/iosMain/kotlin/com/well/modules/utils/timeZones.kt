package com.well.modules.utils

import platform.Foundation.NSTimeZone
import platform.Foundation.knownTimeZoneNames
import platform.Foundation.localTimeZone

actual fun timeZonesIdentifiersList(): Set<String> =
    NSTimeZone.knownTimeZoneNames.map { it.toString() }.toSet()

actual fun currentTimeZoneIdentifier(): String = NSTimeZone.localTimeZone.name