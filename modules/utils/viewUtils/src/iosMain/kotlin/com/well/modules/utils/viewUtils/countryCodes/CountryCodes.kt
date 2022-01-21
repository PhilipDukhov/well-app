package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.utils.viewUtils.SystemContext
import platform.Foundation.*

actual fun countryCodesList(): Set<String> =
    NSLocale.ISOCountryCodes.map { it.toString() }.toSet()

actual fun nameForCountryCode(countryCode: String) =
    NSLocale.systemLocale.displayNameForKey(NSLocaleCountryCode, countryCode)!!

actual fun currentCountryCode(systemContext: SystemContext): String? =
    NSLocale.currentLocale.countryCode