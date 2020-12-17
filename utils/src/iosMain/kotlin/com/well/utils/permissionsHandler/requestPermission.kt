package com.well.utils.permissionsHandler

import com.well.utils.permissionsHandler.PermissionsHandler.Result
import com.well.utils.permissionsHandler.PermissionsHandler.Type.*
import com.well.utils.permissionsHandler.PermissionsHandler.Result.*
import com.well.utils.permissionsHandler.PermissionsHandler.Type
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.*
import kotlin.coroutines.resume

actual suspend fun PermissionsHandler.requestPermissions(vararg types: Type) =
    types.mapWhileAuthorized {
        it to requestPermission(it)
    }

actual suspend fun PermissionsHandler.requestPermission(type: Type): Result = when (type) {
    Camera -> requestAVCaptureDevicePermission(AVMediaTypeVideo)
    Microphone -> requestAVCaptureDevicePermission(AVMediaTypeAudio)
}

private suspend fun requestAVCaptureDevicePermission(mediaType: AVMediaType): Result =
    AVCaptureDevice.run {
        when (authorizationStatusForMediaType(mediaType)) {
            AVAuthorizationStatusNotDetermined -> suspendCancellableCoroutine { continuation ->
                requestAccessForMediaType(mediaType) {
                    continuation.resume(if (it) Authorized else Denied)
                }
            }
            AVAuthorizationStatusDenied -> Denied
            AVAuthorizationStatusAuthorized -> Authorized
            else -> TODO()
        }
    }

private inline fun <T : Type, R : Pair<Type, Result>> Array<out T>.mapWhileAuthorized(transform: (T) -> R): List<R> {
    val destination = ArrayList<R>(size)
    for (item in this) {
        val transformed = transform(item)
        destination.add(transformed)
        if (transformed.second != Authorized) break
    }
    return destination
}