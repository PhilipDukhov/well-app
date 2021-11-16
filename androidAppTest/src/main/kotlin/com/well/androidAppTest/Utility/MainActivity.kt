package com.well.androidAppTest.Utility

import com.well.androidAppTest.TestComposeScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.utils.viewUtils.AppContext
import com.well.modules.utils.viewUtils.ContextHelper
import com.well.modules.utils.viewUtils.napier.NapierProxy
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity() {
    init {
        NapierProxy.initializeLogging()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contextHelper = ContextHelper(AppContext(this))
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Theme {
                ProvideWindowInsets {
                    CompositionLocalProvider(
                        LocalContextHelper provides contextHelper
                    ) {
                        TestComposeScreen()
                    }
                }
            }
        }
    }
}

val LocalContextHelper = compositionLocalOf<ContextHelper> {
    error("CompositionLocal LocalAppContext not present")
}