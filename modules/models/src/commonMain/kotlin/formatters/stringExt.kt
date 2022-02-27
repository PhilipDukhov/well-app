package com.well.modules.models.formatters

fun String.initialsFromName() =
    split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString(separator = "")