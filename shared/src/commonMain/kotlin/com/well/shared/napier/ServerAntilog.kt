package com.well.shared.napier

import com.github.aakira.napier.Antilog
import com.github.aakira.napier.Napier

class ServerAntilog: Antilog() {
    override fun performLog(
        priority: Napier.Level,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        // TODO implement
    }
}