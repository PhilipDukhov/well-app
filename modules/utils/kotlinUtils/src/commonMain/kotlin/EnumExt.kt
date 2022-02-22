package com.well.modules.utils.kotlinUtils

inline fun <reified T : Enum<T>> T.spacedUppercaseName(): String =
    name.mapIndexed { i, c ->
        if (i == 0 || c.isLowerCase() || name[i - 1].isUpperCase()) return@mapIndexed c.toString()
        " $c"
    }.joinToString(separator = "")

inline fun <reified T : Enum<T>> spacedUppercaseEnumValues() =
    enumValues<T>().map { it.spacedUppercaseName() }

inline fun <reified T : Enum<T>> enumValueOfSpacedUppercase(name: String): T =
    enumValues<T>().first { it.spacedUppercaseName() == name }