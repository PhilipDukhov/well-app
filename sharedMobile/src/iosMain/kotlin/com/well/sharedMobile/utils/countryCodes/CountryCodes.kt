package com.well.sharedMobile.utils.countryCodes

import platform.Foundation.*

actual fun countryCodesList(): Set<String> =
    NSLocale.ISOCountryCodes.map { it.toString() }.toSet()

actual fun nameForCountryCode(countryCode: String) =
    NSLocale.systemLocale.displayNameForKey(NSLocaleCountryCode, countryCode)!!