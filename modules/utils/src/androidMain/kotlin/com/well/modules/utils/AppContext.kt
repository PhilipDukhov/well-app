package com.well.modules.utils

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.well.modules.utils.permissionsHandler.PermissionHandlerContext

actual class AppContext(val androidContext: ComponentActivity) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext(androidContext)

    actual fun systemBack() {
        MainScope().launch {
            androidContext.onBackPressedDispatcher.onBackPressed()
        }
    }
}

inline fun <reified T> android.content.Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)