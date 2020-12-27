package com.well.androidApp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.TopLevelScreen
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.ui.videoCall.WebRTCManagerEffectHandler
import com.well.androidApp.utils.Utilities
import com.well.shared.napier.NapierProxy
import com.well.shared.puerh.featureProvider.Context
import com.well.shared.puerh.featureProvider.FeatureProvider
import com.well.shared.puerh.topLevel.TestDevice
import com.well.shared.puerh.topLevel.TopLevelFeature
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import io.ktor.client.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val featureProvider = FeatureProvider(
        Context(this),
        if (Utilities.isProbablyAnEmulator()) TestDevice.Emulator else TestDevice.Android,
        webRTCManagerGenerator = { webSocketManager ->
            WebRTCManagerEffectHandler(
                webSocketManager,
                this,
                CoroutineScope(Dispatchers.IO),
            )
        }
    )

    private var cached: TopLevelFeature.State? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
            ServiceLoader.load(it, it.classLoader).toList()
        }
        println("engines $engines")

        super.onCreate(savedInstanceState)
        NapierProxy.initializeLogging()

        featureProvider.feature.apply {
            listenState {
//                val currentScreen = it.currentScreen
//                if (currentScreen is TopLevelFeature.State.ScreenState.Call
//                    && currentScreen.state.localVideoContext != null
//                ) {
//                    cached = it
//                    CoroutineScope(Dispatchers.IO).launch {
//                        while (true) {
//                            delay(1000)
//                            MainScope().launch {
//                                setContent {
//                                    Theme {
//                                        ProvideWindowInsets {
//                                            TopLevelScreen(it, ::accept)
//                                        }
//                                    }
//                                }
//                                println("updated")
//                            }
//                        }
//                    }
//                } else {
//                    cached = null
                    setContent {
                        Theme {
                            ProvideWindowInsets {
                                TopLevelScreen(it, ::accept)
                            }
                        }
                    }
//                }
            }
        }
    }
}