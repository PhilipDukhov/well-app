package com.well.androidApp

import com.well.modules.androidUi.composableScreens.TopLevelScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.features.topLevel.topLevelHandlers.handleActivityResult
import com.well.modules.features.topLevel.topLevelHandlers.handleOnNewIntent
import com.well.modules.utils.viewUtils.SystemContext
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                featureProvider.accept(TopLevelFeature.Msg.Back)
            }
        })
        setContent {
            Theme {
                val state by featureProvider.state.collectAsState()
                TopLevelScreen(state, featureProvider::accept)
            }
        }
        featureProvider.accept(TopLevelFeature.Msg.UpdateSystemContext(SystemContext(this)))
    }

    override fun onDestroy() {
        super.onDestroy()
        featureProvider.accept(TopLevelFeature.Msg.UpdateSystemContext(null))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        featureProvider.handleOnNewIntent(intent)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (!featureProvider.handleActivityResult(requestCode, resultCode, data)) {
            @Suppress("DEPRECATION")
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}