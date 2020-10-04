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
        if (priority < Napier.Level.ERROR) return
        FirebaseCrashlytics.getInstance().log("$tag : $message")

        throwable?.let {
//            when {
            // e.g. http exception, add a customized your exception message
//                it is KtorException -> {
//                    Crashlytics.getInstance().core.log(priority.ordinal, "HTTP Exception", it.response?.errorBody.toString())
//                }
//            }
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }
}

