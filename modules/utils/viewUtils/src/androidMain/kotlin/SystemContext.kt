package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import androidx.activity.ComponentActivity
import coil.imageLoader
import coil.memory.MemoryCache
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

actual class SystemContext(val activity: ComponentActivity) {
    actual val permissionHandlerContext = PermissionHandlerContext(activity)
    actual val helper = SystemHelper(this)

    actual fun systemBack() {
        MainScope().launch {
            activity.finish()
        }
    }

    actual fun cacheImage(
        image: ImageContainer,
        url: String,
    ) {
        activity.imageLoader
            .memoryCache
            ?.set(
                MemoryCache.Key(url),
                MemoryCache.Value(image.getBitmap()),
            )
    }
}