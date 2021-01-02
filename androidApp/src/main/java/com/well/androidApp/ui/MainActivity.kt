package com.well.androidApp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.TopLevelScreen
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.webRtc.WebRtcManager
import com.well.sharedMobile.napier.NapierProxy
import com.well.sharedMobile.puerh.featureProvider.FeatureProvider
import com.well.utils.Context
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import kotlinx.coroutines.InternalCoroutinesApi

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

    @InternalCoroutinesApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NapierProxy.initializeLogging()

        featureProvider.feature.apply {
            listenState {
                setContent {
                    Theme {
                        ProvideWindowInsets {
                            TopLevelScreen(it, ::accept)
                        }
                    }
                }
            }
        }
    }
}