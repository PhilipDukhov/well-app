package com.well.androidApp.utils

inline fun <I, O> Iterable<I>.firstMapOrNull(predicate: (I) -> O?): O? {
    for (element in this) return predicate(element) ?: continue
    return null
}