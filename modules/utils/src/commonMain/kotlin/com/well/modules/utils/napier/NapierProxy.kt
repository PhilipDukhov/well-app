package com.well.modules.utils.napier

import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object NapierProxy {
    fun initializeLogging() {
        Napier.base(
            if (Platform.isDebug) {
                DebugAntilog()
            } else {
                return
//                FileAntilog()
            }
        )
    }
}