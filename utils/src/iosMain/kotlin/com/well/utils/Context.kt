package com.well.utils

import com.well.utils.permissionsHandler.Context
import platform.UIKit.UIViewController

actual data class Context(val rootController: UIViewController) {
    actual val permissionsHandlerContext: Context
        get() = Context()
}