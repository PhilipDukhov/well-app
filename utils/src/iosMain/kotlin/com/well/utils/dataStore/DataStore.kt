package com.well.utils.dataStore

import com.well.utils.Context
import platform.Foundation.NSUserDefaults

actual class DataStore actual constructor(context: Context) {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    // Fields
    private val deviceUUIDKey = "deviceUUID"
    actual var deviceUUID: String?
        get() = userDefaults.stringForKey(deviceUUIDKey)
        set(value) = userDefaults.setObject(value, deviceUUIDKey)

    private val loginTokenKey = "loginTokenKey"
    actual var loginToken: String?
        get() = userDefaults.stringForKey(loginTokenKey)
        set(value) = userDefaults.setObject(value, loginTokenKey)
}