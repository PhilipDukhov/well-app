package com.well.utils

import androidx.activity.ComponentActivity
import com.well.utils.permissionsHandler.Context

actual class Context(val componentActivity: ComponentActivity) {
    actual val permissionsHandlerContext: Context
        get() = Context(componentActivity)

    actual fun systemBack() {
        componentActivity.onBackPressedDispatcher.onBackPressed()
    }
}