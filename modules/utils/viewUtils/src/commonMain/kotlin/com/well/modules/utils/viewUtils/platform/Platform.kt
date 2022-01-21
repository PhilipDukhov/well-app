package com.well.modules.utils.viewUtils.platform

import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.dataStore.DataStore

class Platform(applicationContext: ApplicationContext) {
    val dataStore = DataStore(applicationContext)

    enum class Platform {
        Ios,
        Android,
    }
    companion object
}