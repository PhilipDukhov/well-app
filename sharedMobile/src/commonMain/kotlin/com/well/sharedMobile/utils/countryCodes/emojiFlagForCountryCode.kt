package com.well.sharedMobile.utils.countryCodes

import com.well.modules.models.User
import com.well.modules.utils.toChars

// TODO 1.5: toInt() -> code
fun emojiFlagForCountryCode(countryCode: String): String =
    (0x1F1E6 - 0x41).let { offset ->
        countryCode.map { Char.toChars(it.toInt() + offset).concatToString() }.joinToString(separator = "")
    }

fun User.countryName() = countryCode?.let(::nameForCountryCode)