package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.permissionsHandler.PermissionHandlerContext
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer

expect class SystemContext {
    val permissionHandlerContext: PermissionHandlerContext
    val helper: SystemHelper
    fun systemBack()
    fun cacheImage(image: ImageContainer, url: String)
}