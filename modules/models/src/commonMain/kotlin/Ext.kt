package com.well.modules.models

fun String.prepareToDebug(): String =
    trim()
        .replace("\n", "\\n")
        .apply {
            val maxLength = 100
            if (length <= maxLength) return this
            val replacement = "..."
            val resultLength = maxLength - replacement.length
            return replaceRange(
                resultLength / 2,
                length - (resultLength + 1) / 2,
                replacement,
            )
        }