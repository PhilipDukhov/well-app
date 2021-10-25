package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer

expect class AppContext {
    val permissionsHandlerContext: PermissionHandlerContext
    fun systemBack()
    fun cacheImage(image: ImageContainer, url: String)
}