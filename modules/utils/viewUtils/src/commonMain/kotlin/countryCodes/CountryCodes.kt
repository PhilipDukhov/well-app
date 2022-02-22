package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.utils.viewUtils.SystemContext

expect fun countryCodesList(): Set<String>

expect fun nameForCountryCode(countryCode: String): String

expect fun currentCountryCode(systemContext: SystemContext): String?