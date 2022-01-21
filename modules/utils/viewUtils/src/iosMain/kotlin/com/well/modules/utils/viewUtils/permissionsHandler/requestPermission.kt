package com.well.modules.utils.viewUtils.permissionsHandler

import com.well.modules.atomic.freeze
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result.Authorized
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Result.Denied
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Type
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Type.CallPhone
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Type.Camera
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler.Type.Microphone
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaType
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import kotlin.coroutines.resume

actual suspend fun PermissionsHandler.requestPermissions(vararg types: Type) =
    types.mapWhileAuthorized {
        it to requestPermission(it)
    }

suspend fun PermissionsHandler.requestPermission(type: Type): Result = when (type) {
    Camera -> requestAVCaptureDevicePermission(AVMediaTypeVideo)
    Microphone -> requestAVCaptureDevicePermission(AVMediaTypeAudio)
    CallPhone -> Authorized
}

private suspend fun requestAVCaptureDevicePermission(mediaType: AVMediaType): Result =
    AVCaptureDevice.run {
        when (val status = authorizationStatusForMediaType(mediaType)) {
            AVAuthorizationStatusNotDetermined -> suspendCancellableCoroutine { continuation ->
                requestAccessForMediaType(mediaType, { success: Boolean ->
                    continuation.resume(if (success) Authorized else Denied)
                }.freeze())
            }
            AVAuthorizationStatusDenied -> Denied
            AVAuthorizationStatusAuthorized -> Authorized
            else -> throw IllegalStateException("AVAuthorizationStatus $status unhandled")
        }
    }

private suspend fun <T : Type, R : Pair<Type, Result>> Array<out T>.mapWhileAuthorized(transform: suspend (T) -> R): List<R> {
    val destination = ArrayList<R>(size)
    for (item in this) {
        val transformed = transform(item)
        destination.add(transformed)
        if (transformed.second != Authorized) break
    }
    return destination
}