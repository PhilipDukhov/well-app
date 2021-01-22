package com.well.androidApp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.TestComposeScreen
import com.well.androidApp.ui.composableScreens.TopLevelScreen
import com.well.androidApp.ui.composableScreens.testing
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.webRtc.WebRtcManager
import com.well.sharedMobile.napier.NapierProxy
import com.well.sharedMobile.puerh.featureProvider.FeatureProvider
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature
import com.well.utils.Context
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val featureProvider = FeatureProvider(
        Context(this),
        webRtcManagerGenerator = { iceServers, listener ->
            WebRtcManager(
                iceServers,
                applicationContext,
                listener,
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NapierProxy.initializeLogging()

        featureProvider.feature.apply {
            listenState {
                setContent {
                    Theme {
                        ProvideWindowInsets {
                            if (testing) {
                                TestComposeScreen()
                            } else {
                                TopLevelScreen(it, ::accept)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        featureProvider.feature.accept(TopLevelFeature.Msg.Back)
    }
}