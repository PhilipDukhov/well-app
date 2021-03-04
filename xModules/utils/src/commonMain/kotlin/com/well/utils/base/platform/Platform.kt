package com.well.utils.platform

import com.well.utils.Context
import com.well.utils.dataStore.DataStore

class Platform(context: Context) {
    val dataStore = DataStore(context)

    enum class Platform {
        Ios,
        Android,
    }
    companion object
}