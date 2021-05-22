package com.well.androidApp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import com.well.androidApp.R
import com.well.androidApp.call.webRtc.WebRtcManager
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.composableScreens.πCustomViews.LocalBackPressedDispatcher
import com.well.sharedMobile.puerh._featureProvider.FeatureProvider
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.sharedMobile.puerh.login.SocialNetwork
import com.well.sharedMobile.puerh.login.credentialProviders.FacebookProvider
import com.well.sharedMobile.puerh.login.credentialProviders.GoogleProvider
import com.well.sharedMobile.puerh.login.handleActivityResult
import com.well.sharedMobile.utils.napier.NapierProxy
import com.well.modules.utils.Context
import com.google.accompanist.insets.ProvideWindowInsets
import com.well.sharedMobile.puerh.login.credentialProviders.AppleOAuthProvider
import com.well.sharedMobile.puerh.login.handleOnNewIntent
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    init {
        NapierProxy.initializeLogging()
    }

    private val featureProvider = FeatureProvider(
        Context(this),
        webRtcManagerGenerator = { iceServers, listener ->
            WebRtcManager(
                iceServers,
                applicationContext,
                listener,
            )
        },
        providerGenerator = { socialNetwork, context ->
            when (socialNetwork) {
                SocialNetwork.Facebook -> FacebookProvider(context)
                SocialNetwork.Google -> GoogleProvider(
                    context = context,
                    tokenId = resources.getString(R.string.google_web_client_id)
                )
                SocialNetwork.Apple -> AppleOAuthProvider(
                    context = context,
                    clientId = resources.getString(R.string.apple_server_client_id),
                    redirectUri = resources.getString(R.string.apple_auth_redirect_url),
                )
                SocialNetwork.Twitter
                -> throw IllegalStateException("Twitter should be handler earlier")
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("MainActivity onCreate ${intent.dataString}")

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

    override fun onBackPressed() {
        featureProvider.feature.accept(TopLevelFeature.Msg.Back)
    }
}
