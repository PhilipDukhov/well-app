package com.well.modules.utils.dbUtils

inline fun <reified T : Enum<T>> Set<Enum<T>>.adaptedIntersectionRegex() =
    adaptedIntersectionRegex { it.name }

fun<E> Collection<E>.adaptedIntersectionRegex(transform: (E) -> CharSequence) =
    if (isEmpty())
        ""
    else
        joinToString(
            separator = "|",
            prefix = "(,|^)(",
            postfix = "(,|$))"
        ) { transform(it) }

fun Collection<String>.adaptedOneOfRegex() =
    if (isEmpty())
        ""
    else
        joinToString(
            separator = "|",
            prefix = "^(",
            postfix = ")$"
        )