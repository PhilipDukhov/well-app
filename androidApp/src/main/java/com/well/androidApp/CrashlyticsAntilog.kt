package com.well.androidApp

import android.content.Context
import com.github.aakira.napier.Antilog
import com.github.aakira.napier.Napier
import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsAntilog(private val context: Context) : Antilog() {
    override fun performLog(
        priority: Napier.Level,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        if (priority < Napier.Level.ERROR) {
            return
        }
        FirebaseCrashlytics.getInstance().log("$tag : $message")

        throwable?.let {
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }
}
