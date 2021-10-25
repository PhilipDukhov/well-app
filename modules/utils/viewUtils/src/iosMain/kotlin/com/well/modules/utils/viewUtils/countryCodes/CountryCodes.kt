package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.utils.viewUtils.AppContext
import platform.Foundation.*

actual fun countryCodesList(): Set<String> =
    NSLocale.ISOCountryCodes.map { it.toString() }.toSet()

actual fun nameForCountryCode(countryCode: String) =
    NSLocale.systemLocale.displayNameForKey(NSLocaleCountryCode, countryCode)!!

actual fun currentCountryCode(appContext: AppContext): String? =
    NSLocale.currentLocale.countryCode