package com.well.utils.dataStore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import com.well.utils.Context
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

actual class DataStore actual constructor(context: Context) {
    private val dataStore = context.componentActivity.createDataStore(
        name = "settings"
    )

    internal actual inline fun <reified T> getValue(key: Key<T>): T? =runBlocking {
        dataStore.data.map { it[key] }
            .firstOrNull()
    }

    internal actual inline fun <reified T> setValue(
        value: T?,
        key: Key<T>
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