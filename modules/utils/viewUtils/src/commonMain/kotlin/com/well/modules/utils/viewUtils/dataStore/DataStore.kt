package com.well.modules.utils.viewUtils.dataStore

import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
private val authInfoKey = createKey<String>(if (Platform.isDebug) "debugAuthInfoKey" else "authInfoKey")
var DataStore.authInfo: AuthInfo?
    get() = getValue(authInfoKey)?.let { Json.decodeFromString(it) }
    set(value) = setValue(Json.encodeToString(value), authInfoKey)

@SharedImmutable
private val welcomeShowedKey = createKey<Boolean>("welcomeShowedKey")
var DataStore.welcomeShowed: Boolean
    get() = getValue(welcomeShowedKey) ?: false
    set(value) = setValue(value, welcomeShowedKey)