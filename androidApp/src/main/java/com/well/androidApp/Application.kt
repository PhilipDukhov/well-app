package com.well.androidApp

import android.app.Application
import com.github.aakira.napier.*
import com.google.firebase.crashlytics.FirebaseCrashlytics

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogging()
    }

    private fun initializeLogging() =
        Napier.base(if (BuildConfig.DEBUG) DebugAntilog() else CrashlyticsAntilog(this))
}