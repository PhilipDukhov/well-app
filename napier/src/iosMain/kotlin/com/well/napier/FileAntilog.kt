package com.well.napier

actual class FileAntilog: Antilog() {
    override fun performLog(
        priority: Napier.Level,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {

    }
}