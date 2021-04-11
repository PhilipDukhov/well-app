package com.well.sharedMobile.utils.napier

import com.well.modules.napier.ConsoleAntilog
import com.well.modules.napier.FileAntilog
import com.well.modules.napier.Napier
import com.well.modules.utils.platform.Platform
import com.well.modules.utils.platform.isDebug

object NapierProxy {
    fun initializeLogging() {
        Napier.base(
            if (Platform.isDebug) {
                ConsoleAntilog()
            } else {
                FileAntilog()
            }
        )
    }
}