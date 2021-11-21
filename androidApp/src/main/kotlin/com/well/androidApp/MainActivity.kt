package com.well.androidApp

import com.well.modules.androidUi.composableScreens.TopLevelScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.androidWebrtc.WebRtcManager
import com.well.modules.features.login.loginHandlers.credentialProviders.providerGenerator
import com.well.modules.puerhBase.FeatureProvider
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.napier.NapierProxy
import com.well.sharedMobile.TopLevelFeature
import com.well.sharedMobile.featureProvider.createFeatureProvider
import com.well.sharedMobile.handleActivityResult
import com.well.sharedMobile.handleOnNewIntent
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity() {
    private lateinit var featureProvider: FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        NapierProxy.initializeLogging()
        featureProvider = createFeatureProvider(
            AppContext(this),
            webRtcManagerGenerator = { iceServers, listener ->
                WebRtcManager(
                    iceServers,
                    applicationContext,
                    listener,
                )
            },
            providerGenerator = ::providerGenerator
        )
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                featureProvider.accept(TopLevelFeature.Msg.Back)
            }
        })
        featureProvider
            .listenState {
                setContent {
                    Theme {
                        ProvideWindowInsets {
                            TopLevelScreen(it, featureProvider::accept)
                        }
                    }
                }
            }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        featureProvider.handleOnNewIntent(intent)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (!featureProvider.handleActivityResult(requestCode, resultCode, data)) {
            @Suppress("DEPRECATION")
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}