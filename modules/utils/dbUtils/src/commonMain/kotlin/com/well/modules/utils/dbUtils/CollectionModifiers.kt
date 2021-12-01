package com.well.modules.utils.dbUtils

inline fun <reified T : Enum<T>> Set<Enum<T>>.adaptedIntersectionRegex() =
    adaptedIntersectionRegex { it.name }

fun Collection<String>.adaptedIntersectionRegex() =
    if (isEmpty())
        ""
    else
        joinToString(
            separator = "|",
            prefix = "(,|^)(",
            postfix = "(,|$))"
        ) { it }

fun Collection<String>.adaptedOneOfRegex() =
    if (isEmpty())
        ""
    else
        joinToString(
            separator = "|",
            prefix = "^(",
            postfix = ")$"
        )