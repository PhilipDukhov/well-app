package com.well.utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.well.utils.permissionsHandler.PermissionHandlerContext

actual class Context(val componentActivity: ComponentActivity) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext(componentActivity)

    actual fun systemBack() {
        MainScope().launch {
            componentActivity.onBackPressedDispatcher.onBackPressed()
        }
    }
}

inline fun <reified T> android.content.Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)