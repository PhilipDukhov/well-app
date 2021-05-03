package com.well.modules.utils.dataStore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.reflect.KClass

actual typealias Key<T> = Preferences.Key<T>
@Suppress("UNCHECKED_CAST")
actual inline fun <reified T : Any> createKey(name: String): Key<T> =
    when (T::class) {
        String::class -> stringPreferencesKey(name) as Preferences.Key<T>
        Boolean::class -> booleanPreferencesKey(name) as Preferences.Key<T>
        else -> throw UnsupportedKeyClass(T::class)
    }

data class UnsupportedKeyClass(val kClass: KClass<*>) : Exception()