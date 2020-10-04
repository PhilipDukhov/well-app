package com.well.androidApp

import android.app.Application
import com.github.aakira.napier.BuildConfig
import com.github.aakira.napier.DebugAntilog
import com.github.aakira.napier.Napier
import com.google.firebase.auth.FirebaseAuth

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initializeLogging()
        FirebaseAuth.getInstance().signOut()
    }

    private fun initializeLogging() =
        Napier.base(if (BuildConfig.DEBUG) DebugAntilog() else CrashlyticsAntilog(this))
}