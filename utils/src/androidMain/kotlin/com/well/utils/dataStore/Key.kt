package com.well.utils.dataStore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

actual typealias Key<T> = Preferences.Key<T>
@Suppress("UNCHECKED_CAST")
actual inline fun <reified T : Any> createKey(name: String): Key<T> =
    when (T::class) {
        String::class -> stringPreferencesKey(name) as Preferences.Key<T>
        else -> throw IllegalStateException()
    }