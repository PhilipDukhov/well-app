package com.well.utils.dataStore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesKey

actual typealias Key<T> = Preferences.Key<T>
actual inline fun <reified T : Any> createKey(name: String): Key<T> =
    preferencesKey(name)