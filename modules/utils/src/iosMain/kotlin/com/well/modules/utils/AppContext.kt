package com.well.modules.utils

import com.well.modules.utils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.sharedImage.ImageContainer
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

actual data class AppContext(
    val rootController: UIViewController,
    val application: UIApplication,
    val launchOptions: Map<Any?, Any>?,
    val cacheImage: (ImageContainer, NSURL) -> Unit
) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext()

    actual fun systemBack() {}
    actual fun cacheImage(image: ImageContainer, url: String) {
        cacheImage(image, NSURL.URLWithString(url)!!)
    }
}