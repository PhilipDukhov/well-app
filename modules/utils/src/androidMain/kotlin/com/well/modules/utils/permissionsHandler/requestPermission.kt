package com.well.modules.utils.permissionsHandler

import android.Manifest.permission.*
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.well.modules.utils.permissionsHandler.PermissionsHandler.Type
import com.well.modules.utils.permissionsHandler.PermissionsHandler.Result
import com.well.modules.utils.permissionsHandler.PermissionsHandler.Result.*
import com.well.modules.utils.permissionsHandler.PermissionsHandler.Type.*
import java.lang.IllegalStateException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun PermissionsHandler.requestPermissions(vararg types: Type): List<Pair<Type, Result>> =
    suspendCoroutine { continuation ->
        context.launchPermissionsHandler(
            types.map {
                when (it) {
                    Camera -> CAMERA
                    Microphone -> RECORD_AUDIO
                }
            }
        ) { result ->
            continuation.resume(result.map {
                Type(it.key) to if (it.value) Authorized else Denied
            })
        }
    }

//actual suspend fun PermissionsHandler.requestPermission(type: Type): Result {
//    val permission = when (type) {
//        Camera -> CAMERA
//        Microphone -> RECORD_AUDIO
//    }
//    if (ContextCompat.checkSelfPermission(
//        context.androidContext,
//        permission
//    ) == PackageManager.PERMISSION_GRANTED) {
//        return Authorized
//    }
//    return suspendCoroutine { continuation ->
//        context.permissionHandler = {
//            continuation.resume(if (it) Authorized else Denied)
//        }
//        launcher.launch(permission)
//    }
//}

private fun Type(permission: String): Type =
    when (permission) {
        CAMERA -> Camera
        RECORD_AUDIO -> Microphone
        else -> throw IllegalStateException()
    }