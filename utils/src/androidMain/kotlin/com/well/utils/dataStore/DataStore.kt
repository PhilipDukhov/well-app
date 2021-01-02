package com.well.utils.dataStore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.createDataStore
import com.well.utils.Context
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

actual class DataStore actual constructor(context: Context) {
    // Props
    private val deviceUuidKey = preferencesKey<String>("deviceUuidKey")
    actual var deviceUUID: String?
        get() = getOrNull(deviceUuidKey)
        set(value) = set(deviceUuidKey, value)

    private val loginTokenKey = preferencesKey<String>("loginTokenKey")
    actual var loginToken: String?
        get() = getOrNull(loginTokenKey)
        set(value) = set(loginTokenKey, value)

    // Helopers
    private val dataStore = context.componentActivity.createDataStore(
        name = "settings"
    )

    private fun <T> getOrNull(key: Preferences.Key<T>): T? = runBlocking {
        dataStore.data.map { it[key] }
            .firstOrNull()
    }

    private fun <T> set(
        key: Preferences.Key<T>,
        value: T?,
    ) {
        runBlocking {
            dataStore.edit {
                if (value != null) {
                    it[key] = value
                } else {
                    it.remove(key)
                }
            }
        }
    }
}
