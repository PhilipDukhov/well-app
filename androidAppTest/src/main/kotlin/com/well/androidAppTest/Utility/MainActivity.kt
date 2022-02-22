package com.well.androidAppTest.Utility

import com.well.androidApp.R
import com.well.androidAppTest.TestComposeScreen
import com.well.modules.androidUi.theme.Theme
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.SystemContext
import com.well.modules.utils.viewUtils.SystemHelper
import com.well.modules.utils.viewUtils.napier.NapierProxy
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.core.view.WindowCompat
import com.google.accompanist.insets.ProvideWindowInsets

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NapierProxy.initializeLogging(ApplicationContext(applicationContext, R.drawable.ic_100tb, MainActivity::class.java))
        val systemHelper = SystemHelper(SystemContext(this))
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            Theme {
                ProvideWindowInsets {
                    CompositionLocalProvider(
                        LocalSystemHelper provides systemHelper
                    ) {
                        TestComposeScreen()
                    }
                }
            }
        }
    }
}

val LocalSystemHelper = compositionLocalOf<SystemHelper> {
    error("CompositionLocal LocalAppContext not present")
}