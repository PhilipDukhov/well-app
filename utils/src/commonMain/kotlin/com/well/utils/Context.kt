package com.well.utils

expect class Context {
    val permissionsHandlerContext: com.well.utils.permissionsHandler.Context
    fun systemBack()
}