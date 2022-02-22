package com.well.modules.utils.viewUtils.napier

import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object NapierProxy {
    fun initializeLogging(applicationContext: ApplicationContext) {
        if (Platform.isDebug) {
            Napier.base(DebugAntilog())
        }
        Napier.base(FileAntilog(applicationContext))
    }
}