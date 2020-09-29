package com.well.shared.napier

import com.github.aakira.napier.*

fun buildDebug() {
    Napier.base(DebugAntilog())
}

fun build(antilog: Antilog = DebugAntilog()) {
    Napier.base(antilog)
}