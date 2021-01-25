package com.well.utils

import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.well.utils.permissionsHandler.Context
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

actual class Context(val componentActivity: ComponentActivity) {
    actual val permissionsHandlerContext: Context
        get() = Context(componentActivity)

    actual fun systemBack() {
        MainScope().launch {
            componentActivity.onBackPressedDispatcher.onBackPressed()
        }
    }
}

inline fun <reified T> android.content.Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)