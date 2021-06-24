package com.well.modules.utils

import com.well.modules.utils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.sharedImage.ImageContainer

expect class AppContext {
    val permissionsHandlerContext: PermissionHandlerContext
    fun systemBack()
    fun cacheImage(image: ImageContainer, url: String)
}