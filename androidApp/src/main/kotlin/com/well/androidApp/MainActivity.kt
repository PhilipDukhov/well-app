package com.well.androidApp

import com.well.modules.androidUi.composableScreens.TopLevelScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.androidWebrtc.WebRtcManager
import com.well.modules.features.login.loginHandlers.credentialProviders.providerGenerator
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelHandlers.createFeatureProvider
import com.well.modules.features.topLevel.topLevelHandlers.handleActivityResult
import com.well.modules.features.topLevel.topLevelHandlers.handleOnNewIntent
import com.well.modules.puerhBase.FeatureProvider
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.napier.NapierProxy
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
        setContent {
            Theme {
                ProvideWindowInsets {
                    val state by featureProvider.state.collectAsState()
                    TopLevelScreen(state, featureProvider::accept)
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