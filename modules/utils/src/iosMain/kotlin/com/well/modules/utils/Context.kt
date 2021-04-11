package com.well.modules.utils

import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import com.well.modules.utils.permissionsHandler.PermissionHandlerContext

actual data class Context(
    val rootController: UIViewController,
    val application: UIApplication,
    val launchOptions: Map<Any?, Any>?,
) {
    actual val permissionsHandlerContext: PermissionHandlerContext
        get() = PermissionHandlerContext()

    actual fun systemBack() {}
}