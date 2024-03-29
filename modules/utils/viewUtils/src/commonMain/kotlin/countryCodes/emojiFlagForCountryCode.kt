package com.well.modules.utils.viewUtils.countryCodes

import com.well.modules.models.User
import com.well.modules.utils.viewUtils.toChars

fun emojiFlagForCountryCode(countryCode: String): String =
    (0x1F1E6 - 0x41).let { offset ->
        countryCode.map { Char.toChars(it.code + offset).concatToString() }.joinToString(separator = "")
    }

fun User.countryName() = countryCode?.let(::nameForCountryCode)