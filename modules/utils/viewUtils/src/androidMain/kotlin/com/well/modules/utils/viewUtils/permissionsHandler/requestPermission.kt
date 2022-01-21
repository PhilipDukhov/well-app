package com.well.modules.utils.viewUtils.permissionsHandler

import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result.Authorized
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result.Denied
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Type
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual suspend fun PermissionsHandler.requestPermissions(vararg types: Type): List<Pair<Type, Result>> =
    suspendCoroutine { continuation ->
        context.launchPermissionsHandler(
            types.flatMap {
                when (it) {
                    Type.Camera -> listOf(Manifest.permission.CAMERA)
                    Type.Microphone -> listOf(Manifest.permission.RECORD_AUDIO)
                    Type.CallPhone -> listOfNotNull(
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.BIND_TELECOM_CONNECTION_SERVICE,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Manifest.permission.MANAGE_OWN_CALLS else null,
                    )
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
        Manifest.permission.CAMERA -> Type.Camera
        Manifest.permission.RECORD_AUDIO -> Type.Microphone
        Manifest.permission.CALL_PHONE,
        Manifest.permission.MANAGE_OWN_CALLS,
        Manifest.permission.BIND_TELECOM_CONNECTION_SERVICE,
        -> Type.CallPhone
        else -> throw IllegalStateException()
    }