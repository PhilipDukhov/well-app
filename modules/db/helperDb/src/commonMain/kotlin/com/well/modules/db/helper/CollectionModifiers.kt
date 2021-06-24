package com.well.modules.db.helper

inline fun <reified T : Enum<T>> Set<Enum<T>>.adaptedIntersectionRegex() =
    map { it.name }.adaptedIntersectionRegex()

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