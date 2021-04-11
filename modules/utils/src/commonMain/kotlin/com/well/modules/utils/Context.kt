package com.well.modules.utils

import com.well.modules.utils.permissionsHandler.PermissionHandlerContext

expect class Context {
    val permissionsHandlerContext: PermissionHandlerContext
    fun systemBack()
}