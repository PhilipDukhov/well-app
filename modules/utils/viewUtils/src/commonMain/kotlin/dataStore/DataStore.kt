package com.well.modules.utils.viewUtils.dataStore

import com.well.modules.models.DeviceId
import com.well.modules.models.NotificationToken
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isLocalServer
import com.well.modules.utils.viewUtils.randomUUIDString
import io.github.aakira.napier.Napier
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.native.concurrent.SharedImmutable
import kotlin.reflect.KClass

expect class DataStore(applicationContext: ApplicationContext) {
    internal inline fun <reified T> getValue(key: Key<T>): T?
    internal inline fun <reified T> setValue(
        value: T?,
        key: Key<T>,
    )
}

expect class Key<T> {
    val name: String
}

expect inline fun <reified T : Any> createKey(name: String): Key<T>

@SharedImmutable
private val authInfoKey =
    createKey<String>(if (Platform.isLocalServer) "debugAuthInfoKey" else "authInfoKey")
var DataStore.authInfo: AuthInfo?
    get() = getValue(authInfoKey)?.let { Json.decodeFromString(it) }
    set(value) = setValue(Json.encodeToString(value), authInfoKey)

@SharedImmutable
private val welcomeShowedKey = createKey<Boolean>("welcomeShowedKey")
var DataStore.welcomeShowed: Boolean
    get() = getValue(welcomeShowedKey) ?: false
    set(value) = setValue(value, welcomeShowedKey)

@SharedImmutable
private val notificationTokenKey = createKey<String>("notificationTokenKey")
var DataStore.notificationToken: NotificationToken?
    get() = try {
        getValue(notificationTokenKey)?.let(Json::decodeFromString)
    } catch (_: SerializationException) {
        Napier.i("notificationToken deserialization failed")
        null
    }
    set(value) = setValue(value?.let { Json.encodeToString(it) }, notificationTokenKey)

@SharedImmutable
private val notificationTokenNotifiedKey = createKey<Boolean>("notificationTokenNotifiedKey")
var DataStore.notificationTokenNotified: Boolean
    get() = getValue(notificationTokenNotifiedKey) ?: true
    set(value) = setValue(value, notificationTokenNotifiedKey)

@SharedImmutable
private val deviceUidKey = createKey<String>("deviceUidKey")
val DataStore.deviceUid: DeviceId
    get() = DeviceId(
        getValue(deviceUidKey) ?: randomUUIDString().also {
            setValue(it, deviceUidKey)
        }
    )

inline fun<reified E: Exception, R> runIgnoring(block: () -> R) : R? =
    try {
        block()
    } catch (e: Exception) {
        if (e !is E) {
            throw e
        }
        null
    }

