package com.well.androidApp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import com.well.androidApp.R
import com.well.androidApp.call.webRtc.WebRtcManager
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.sharedMobile.puerh._featureProvider.FeatureProvider
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature
import com.well.utils.Context
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import java.util.*

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

    override fun onBackPressed() {
        featureProvider.feature.accept(TopLevelFeature.Msg.Back)
    }
}