package com.well.utils

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import com.well.utils.permissionsHandler.PermissionHandlerContext

actual data class Context(
    val rootController: UIViewController,
    val application: UIApplication,
    val launchOptions: Map<Any?, Any>?,
) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext()

    actual fun systemBack() {}
}