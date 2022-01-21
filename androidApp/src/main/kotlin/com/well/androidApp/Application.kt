package com.well.androidApp

import com.well.modules.androidWebrtc.WebRtcManager
import com.well.modules.features.login.loginHandlers.credentialProviders.providerGenerator
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelHandlers.createFeatureProvider
import com.well.modules.puerhBase.FeatureProvider
import com.well.modules.utils.viewUtils.ApplicationContext
import android.app.Activity
import android.app.Application
import android.app.Service
import com.google.firebase.FirebaseApp

class Application: Application() {
    lateinit var featureProvider: FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>

    override fun onCreate() {
        super.onCreate()

        val a = MainActivity::class.java

        featureProvider = createFeatureProvider(
            applicationContext = ApplicationContext(
                context = applicationContext,
                notificationResId = R.drawable.ic_notification,
                activityClass = MainActivity::class.java,
            ),
            webRtcManagerGenerator = { iceServers, listener ->
                WebRtcManager(
                    iceServers,
                    applicationContext,
                    listener,
                )
            },
            providerGenerator = ::providerGenerator,
        )
        FirebaseApp.initializeApp(this)
    }
}

val Service.featureProvider get() = (application as com.well.androidApp.Application).featureProvider
val Activity.featureProvider get() = (application as com.well.androidApp.Application).featureProvider