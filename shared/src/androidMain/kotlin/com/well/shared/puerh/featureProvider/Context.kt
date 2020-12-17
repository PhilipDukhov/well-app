package com.well.shared.puerh.featureProvider

import androidx.activity.ComponentActivity
import com.well.utils.permissionsHandler.Context

actual class Context(private val componentActivity: ComponentActivity) {
    actual val permissionsHandlerContext: Context
        get() = Context(componentActivity)
}