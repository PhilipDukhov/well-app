package com.well.modules.utils

import com.well.modules.utils.permissionsHandler.PermissionHandlerContext

expect class AppContext {
    val permissionsHandlerContext: PermissionHandlerContext
    fun systemBack()
}