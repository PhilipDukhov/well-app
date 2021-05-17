package com.well.sharedMobile.utils.countryCodes

import com.well.modules.utils.toChars

fun emojiFlagForCountryCode(countryCode: String): String =
    (0x1F1E6 - 0x41).let { offset ->
        countryCode.map { Char.toChars(it.code + offset).concatToString() }.joinToString(separator = "")
    }