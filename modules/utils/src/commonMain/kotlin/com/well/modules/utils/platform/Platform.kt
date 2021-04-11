package com.well.modules.utils.platform

import com.well.modules.utils.Context
import com.well.modules.utils.dataStore.DataStore

class Platform(context: Context) {
    val dataStore = DataStore(context)

    enum class Platform {
        Ios,
        Android,
    }
    companion object
}