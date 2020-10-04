package com.well.shared.napier

import com.github.aakira.napier.Antilog
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier

fun buildDebug() {
    Napier.base(DebugAntilog())
}

fun build(antilog: Antilog = DebugAntilog()) {
    Napier.base(antilog)
}