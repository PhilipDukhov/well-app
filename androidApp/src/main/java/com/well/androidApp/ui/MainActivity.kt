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
import com.well.utils.Context
import com.google.accompanist.insets.ProvideWindowInsets

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
                SocialNetwork.Google -> GoogleProvider(
                    context,
                    resources.getString(R.string.google_web_client_id)
                )
                SocialNetwork.Facebook -> FacebookProvider(context)
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
