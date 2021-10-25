package com.well.modules.utils.viewUtils.permissionsHandler

class PermissionsHandler(internal val context: PermissionHandlerContext) {
    enum class Type {
        Camera,
        Microphone,
    }
    enum class Result {
        Authorized,
        Denied,
    }
}

expect suspend fun PermissionsHandler.requestPermissions(vararg types: PermissionsHandler.Type): List<Pair<PermissionsHandler.Type, PermissionsHandler.Result>>

//expect suspend fun PermissionsHandler.requestPermission(type: PermissionsHandler.Type): PermissionsHandler.Result