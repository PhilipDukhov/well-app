package com.well.androidApp.ui

import com.well.androidApp.R
import com.well.androidApp.call.webRtc.WebRtcManager
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.composableScreens.πCustomViews.LocalBackPressedDispatcher
import com.well.modules.utils.AppContext
import com.well.sharedMobile.puerh._featureProvider.FeatureProvider
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.login.credentialProviders.AppleOAuthProvider
import com.well.sharedMobile.puerh.login.credentialProviders.FacebookProvider
import com.well.sharedMobile.puerh.login.credentialProviders.GoogleProvider
import com.well.sharedMobile.puerh.login.handleActivityResult
import com.well.sharedMobile.puerh.login.handleOnNewIntent
import com.well.sharedMobile.utils.napier.NapierProxy
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    init {
        NapierProxy.initializeLogging()
    }

    private val featureProvider = FeatureProvider(
        AppContext(this),
        webRtcManagerGenerator = { iceServers, listener ->
            WebRtcManager(
                iceServers,
                applicationContext,
                listener,
            )
        },
        providerGenerator = { socialNetwork, context, webAuthenticator ->
            when (socialNetwork) {
                SocialNetwork.Facebook -> FacebookProvider(context)
                SocialNetwork.Google -> GoogleProvider(context)
                SocialNetwork.Apple -> AppleOAuthProvider(
                    appContext = context,
                    webAuthenticator = webAuthenticator,
                )
                SocialNetwork.Twitter
                -> throw IllegalStateException("Twitter should be handler earlier")
            }
        }
    )

    init {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                featureProvider.feature.accept(TopLevelFeature.Msg.Back)
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        featureProvider.feature
            .listenState {
                setContent {
                    Theme {
                        ProvideWindowInsets {
                            CompositionLocalProvider(
                                LocalBackPressedDispatcher provides onBackPressedDispatcher,
                            ) {
                                TopLevelScreen(it, featureProvider.feature::accept)
                            }
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
