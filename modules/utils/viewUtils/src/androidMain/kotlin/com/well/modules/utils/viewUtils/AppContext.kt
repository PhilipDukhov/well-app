package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import coil.Coil
import coil.memory.MemoryCache
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

actual class AppContext(val androidContext: ComponentActivity) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext(androidContext)

    actual fun systemBack() {
        MainScope().launch {
            androidContext.finish()
        }
    }

    actual fun cacheImage(
        image: ImageContainer,
        url: String
    ) {
        Coil.imageLoader(androidContext).memoryCache[MemoryCache.Key(url)] = image.getBitmap()
    }
}

inline fun <reified T> android.content.Context.getSystemService(): T? =
    ContextCompat.getSystemService(this, T::class.java)