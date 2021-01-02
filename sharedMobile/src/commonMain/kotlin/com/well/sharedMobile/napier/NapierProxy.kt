package com.well.sharedMobile.napier

import com.github.aakira.napier.Antilog
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object NapierProxy {
    fun initializeLogging() {
        if (Platform.isDebug) {
            build(DebugAntilog())
        } else {
            build(ServerAntilog())
        }
    }

    private fun build(antilog: Antilog) {
        Napier.base(antilog)
        listOf(
            Dispatchers.Main,
            Dispatchers.Default + SupervisorJob(),
        ).forEach {
            CoroutineScope(it).launch {
                Napier.base(antilog)
            }
        }
    }
}