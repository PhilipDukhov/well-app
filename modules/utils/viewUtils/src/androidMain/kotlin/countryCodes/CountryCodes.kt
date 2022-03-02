package com.well.modules.utils.viewUtils.countryCodes

import android.os.Build
import com.well.modules.utils.viewUtils.SystemContext
import java.util.*

actual fun countryCodesList(): Set<String> =
    Locale.getISOCountries().toSet()

actual fun currentCountryCode(systemContext: SystemContext): String? =
    systemContext.activity.resources.configuration
        .run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locales[0]
            } else {
                @Suppress("DEPRECATION")
                locale
            }
        }?.country?.ifEmpty { null }

actual fun nameForCountryCode(countryCode: String): String =
    Locale("", countryCode).displayName