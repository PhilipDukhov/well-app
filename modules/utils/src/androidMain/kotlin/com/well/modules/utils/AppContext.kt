package com.well.modules.utils

import com.well.modules.utils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.sharedImage.ImageContainer
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

actual class AppContext(val androidContext: ComponentActivity) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext(androidContext)

    actual fun systemBack() {
        MainScope().launch {
            androidContext.onBackPressedDispatcher.onBackPressed()
        }
    }

    actual fun cacheImage(
        image: ImageContainer,
        url: String
    ) {
        TODO()
    }
}

inline fun <reified T> android.content.Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)