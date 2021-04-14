package com.well.modules.utils.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.well.modules.utils.Context
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

actual class DataStore actual constructor(context: Context) {
    private val androidContext = context.componentActivity

    internal actual inline fun <reified T> getValue(key: Key<T>): T? =runBlocking {
        androidContext.dataStore.data.map { it[key] }
            .firstOrNull()
    }

    internal actual inline fun <reified T> setValue(
        value: T?,
        key: Key<T>
    ) {
        runBlocking {
            androidContext.dataStore.edit {
                if (value != null) {
                    it[key] = value
                } else {
                    it.remove(key)
                }
            }
        }
    }

    private val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
}
