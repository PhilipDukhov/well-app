package com.well.modules.utils.countryCodes

import com.well.modules.utils.AppContext

expect fun countryCodesList(): Set<String>

expect fun nameForCountryCode(countryCode: String): String

expect fun currentCountryCode(appContext: AppContext): String?