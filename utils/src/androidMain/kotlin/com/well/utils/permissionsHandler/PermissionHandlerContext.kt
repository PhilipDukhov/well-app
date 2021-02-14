package com.well.utils.permissionsHandler

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

typealias MultiplePermissionsHandler = (Map<String, Boolean>) -> Unit
//typealias SinglePermissionsHandler = (Boolean) -> Unit

actual class PermissionHandlerContext(componentActivity: ComponentActivity) {
    private lateinit var multiplePermissionsHandler: MultiplePermissionsHandler
//    private lateinit var permissionHandler: SinglePermissionsHandler

    private val multiplePermissionsLauncher = componentActivity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { multiplePermissionsHandler(it) }

//    private val permissionLauncher = componentActivity.registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { permissionHandler(it) }

    fun launchPermissionsHandler(
        permissions: List<String>,
        handler: MultiplePermissionsHandler
    ) {
        multiplePermissionsHandler = handler
        multiplePermissionsLauncher.launch(permissions.toTypedArray())
    }
}