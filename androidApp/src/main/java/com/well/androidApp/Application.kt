package com.well.androidApp

import android.app.Application
import com.github.aakira.napier.*
import com.google.firebase.crashlytics.FirebaseCrashlytics

class App : Application() {
    override fun onCreate() {
        super.onCreate()

//        if (BuildConfig.DEBUG) {
//            Napier.base(DebugAntilog())
//        } else {
            Napier.base(CrashlyticsAntilog(this))
//        }
    }
}

//if (BuildConfig.DEBUG) {
//    // Debug build
//
//    // init napier
//    Napier.base(DebugAntilog())
//} else {
//    // Others(Release build)
//
//    // init firebase crashlytics
//    Fabric.with(this, Crashlytics())
//    // init napier
//    Napier.base(CrashlyticsAntilog(this))
//}