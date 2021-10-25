package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.utils.viewUtils.AppContext

expect fun countryCodesList(): Set<String>

expect fun nameForCountryCode(countryCode: String): String

expect fun currentCountryCode(appContext: AppContext): String?