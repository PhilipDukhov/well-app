package com.well.modules.utils.dataStore

import com.well.modules.utils.AppContext
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import kotlin.native.concurrent.SharedImmutable

expect class DataStore(appContext: AppContext) {
    internal inline fun <reified T> getValue(key: Key<T>): T?
    internal inline fun <reified T> setValue(
        value: T?,
        key: Key<T>
    )
}

expect class Key<T> {
    val name: String
}

expect inline fun <reified T : Any> createKey(name: String): Key<T>

@SharedImmutable
internal val deviceUuidKey = createKey<String>("deviceUuidKey")
var DataStore.deviceUUID: String?
    get() = getValue(deviceUuidKey)
    set(value) = setValue(value, deviceUuidKey)

@SharedImmutable
internal val authTokenKey = createKey<String>(if (Platform.isDebug) "debugAuthTokenKey" else "authTokenKey")
var DataStore.authToken: String?
    get() = getValue(authTokenKey)
    set(value) = setValue(value, authTokenKey)

@SharedImmutable
internal val welcomeShowedKey = createKey<Boolean>("welcomeShowedKey")
var DataStore.welcomeShowed: Boolean
    get() = getValue(welcomeShowedKey) ?: false
    set(value) = setValue(value, welcomeShowedKey)