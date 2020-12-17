package com.well.shared.puerh.featureProvider

import com.well.utils.permissionsHandler.Context

actual class Context {
    actual val permissionsHandlerContext: Context
        get() = Context()
}