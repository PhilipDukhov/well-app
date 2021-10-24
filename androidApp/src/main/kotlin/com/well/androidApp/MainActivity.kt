package com.well.androidApp

import com.well.modules.androidWebrtc.WebRtcManager
import com.well.modules.androidUi.composableScreens.TopLevelScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.features.login.SocialNetwork
import com.well.modules.features.login.credentialProviders.AppleOAuthProvider
import com.well.modules.features.login.credentialProviders.FacebookProvider
import com.well.modules.features.login.credentialProviders.GoogleProvider
import com.well.modules.features.login.credentialProviders.providerGenerator
import com.well.modules.utils.AppContext
import com.well.sharedMobile.TopLevelFeature
import com.well.modules.utils.napier.NapierProxy
import com.well.modules.utils.puerh.FeatureProvider
import com.well.sharedMobile.featureProvider.createFeatureProvider
import com.well.sharedMobile.handleActivityResult
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets
import com.well.sharedMobile.handleOnNewIntent

class MainActivity : AppCompatActivity() {
    init {
        NapierProxy.initializeLogging()
    }

    private lateinit var featureProvider: FeatureProvider<TopLevelFeature.Msg, TopLevelFeature.State>

    init {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                featureProvider.accept(TopLevelFeature.Msg.Back)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
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
        data: Intent?
    ) {
        if (!featureProvider.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}