package com.well.modules.utils.viewUtils.napier

import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object NapierProxy {
    fun initializeLogging() {
        Napier.base(
            if (Platform.isDebug) {
                DebugAntilog()
            } else {
                DebugAntilog()
//                FileAntilog()
            }
        )
    }
}