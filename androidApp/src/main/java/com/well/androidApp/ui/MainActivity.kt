package com.well.androidApp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.TopLevelScreen
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.webRtc.WebRtcManager
import com.well.androidApp.utils.Utilities
import com.well.shared.napier.NapierProxy
import com.well.shared.puerh.featureProvider.Context
import com.well.shared.puerh.featureProvider.FeatureProvider
import com.well.shared.puerh.topLevel.TestDevice
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import io.ktor.client.*
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val featureProvider = FeatureProvider(
        Context(this),
        if (Utilities.isProbablyAnEmulator()) TestDevice.Emulator else TestDevice.Android,
        webRtcManagerGenerator = { listener ->
            WebRtcManager(
                applicationContext,
                listener,
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
            ServiceLoader.load(it, it.classLoader).toList()
        }
        println("engines $engines")

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