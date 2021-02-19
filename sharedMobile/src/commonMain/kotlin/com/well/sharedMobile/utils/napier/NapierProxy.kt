package com.well.sharedMobile.utils.napier

import com.well.napier.ConsoleAntilog
import com.well.napier.FileAntilog
import com.well.napier.Napier
import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug

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