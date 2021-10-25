package com.well.modules.utils.viewUtils.platform

import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.dataStore.DataStore

class Platform(appContext: AppContext) {
    val dataStore = DataStore(appContext)

    enum class Platform {
        Ios,
        Android,
    }
    companion object
}