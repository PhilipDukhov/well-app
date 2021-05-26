package com.well.modules.utils.dataStore

import com.well.modules.utils.AppContext
import platform.Foundation.NSUserDefaults

actual class DataStore actual constructor(appContext: AppContext) {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    internal actual inline fun <reified T> getValue(
        key: Key<T>
    ): T? = when (T::class) {
        String::class -> userDefaults.stringForKey(key.name) as T?
        Boolean::class -> userDefaults.boolForKey(key.name) as T
        else -> throw IllegalStateException()
    }

    internal actual inline fun <reified T> setValue(
        value: T?,
        key: Key<T>
    ) = when (T::class) {
        String::class,
        Boolean::class,
        -> {
            userDefaults.setObject(value, key.name)
        }
        else -> throw IllegalStateException()
    }
}

actual data class Key<T>(actual val name: String)

actual inline fun <reified T : Any> createKey(name: String) = Key<T>(name)