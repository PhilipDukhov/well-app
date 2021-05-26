package com.well.modules.utils.platform

import com.well.modules.utils.AppContext
import com.well.modules.utils.dataStore.DataStore

class Platform(appContext: AppContext) {
    val dataStore = DataStore(appContext)

    enum class Platform {
        Ios,
        Android,
    }
    companion object
}