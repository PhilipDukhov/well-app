package com.well.utils.dataStore

import com.well.utils.Context

expect class DataStore(context: Context) {
    var deviceUUID: String?
    var loginToken: String?
}