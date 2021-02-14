package com.well.utils

import com.well.utils.permissionsHandler.PermissionHandlerContext

expect class Context {
    val permissionsHandlerContext: PermissionHandlerContext
    fun systemBack()
}