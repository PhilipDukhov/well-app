package com.well.androidApp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent
import com.well.androidApp.R
import com.well.androidApp.ui.composableScreens.TopLevelScreen
import com.well.androidApp.ui.composableScreens.theme.Theme
import com.well.androidApp.utils.Utilities
import com.well.shared.napier.NapierProxy
import com.well.shared.puerh.featureProvider.Context
import com.well.shared.puerh.featureProvider.FeatureProvider
import com.well.shared.puerh.topLevel.TestDevice
import com.well.shared.puerh.topLevel.TopLevelFeature
import com.well.shared.puerh.topLevel.TopLevelFeature.Alert.Action.*
import com.well.shared.puerh.topLevel.TopLevelFeature.Eff
import com.well.shared.puerh.topLevel.TopLevelFeature.Msg
import com.well.utils.ExecutorEffectHandler
import com.well.utils.ExecutorEffectsInterpreter
import com.well.utils.wrapWithEffectHandler
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.util.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val featureProvider = FeatureProvider(
        Context(this),
        if (Utilities.isProbablyAnEmulator()) TestDevice.Emulator else TestDevice.Android
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val engines: List<HttpClientEngineContainer> = HttpClientEngineContainer::class.java.let {
            ServiceLoader.load(it, it.classLoader).toList()
        }
        println("engines $engines")

        super.onCreate(savedInstanceState)
        NapierProxy.initializeLogging()

        featureProvider.feature.apply {
            wrapWithEffectHandler(
                ExecutorEffectHandler(
                    alertEffectInterpreter,
                    CoroutineScope(Dispatchers.Main),
                )
            )
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

    private val alertEffectInterpreter: ExecutorEffectsInterpreter<Eff, Msg> =
        { eff, _ ->
            if (eff is Eff.ShowAlert) {
                alertDialogBuilder(eff.alert)
                    .create()
                    .show()
            }
        }

    private fun alertDialogBuilder(alert: TopLevelFeature.Alert) =
        AlertDialog.Builder(this@MainActivity)
            .setMessage(alert.description)
            .setPositiveButton(alert.positiveAction)
            .setNegativeButton(alert.negativeAction)

    private fun AlertDialog.Builder.setPositiveButton(action: TopLevelFeature.Alert.Action) =
        apply {
            setPositiveButton(action.title) { _, _ -> action.handle() }
        }

    private fun AlertDialog.Builder.setNegativeButton(action: TopLevelFeature.Alert.Action) =
        apply {
            setNegativeButton(action.title) { _, _ -> action.handle() }
        }

    private fun TopLevelFeature.Alert.Action.handle() = when (this) {
        Ok -> Unit
        Settings -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        })
    }
}
