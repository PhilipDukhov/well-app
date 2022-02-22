package com.well.modules.utils.viewUtils

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSError
import platform.Foundation.NSFileCoordinator
import platform.Foundation.NSFileCoordinatorReadingForUploading
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.URLByAppendingPathComponent
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class ApplicationContext(val application: UIApplication) {
    actual val documentsDir
        get() = NSTemporaryDirectory()

    actual suspend fun collectLogs(): Path =
        suspendCoroutine { continuation ->
            memScoped {
                val coordinateError = alloc<ObjCObjectVar<NSError?>>()
                NSFileCoordinator().coordinateReadingItemAtURL(
                    url = NSURL.fileURLWithPath(logsDir.toString()),
                    options = NSFileCoordinatorReadingForUploading,
                    error = coordinateError.ptr,
                ) { url ->
                    println("original url $logsDir -> coordinateReadingItemAtURL $url")
                    val output = NSURL.fileURLWithPath(NSTemporaryDirectory())
                        .URLByAppendingPathComponent("logs.zip")!!
                    val moveError = alloc<ObjCObjectVar<NSError?>>()
                    NSFileManager.defaultManager.run {
                        removeItemAtURL(output, null)
                        moveItemAtURL(
                            srcURL = url!!,
                            toURL = output,
                            error = moveError.ptr
                        )
                    }
                    val e = moveError.value
                    if (e != null) {
                        continuation.resumeWithError(e)
                    } else {
                        continuation.resume(output.path!!.toPath())
                    }
                }
                coordinateError.value?.let(continuation::resumeWithError)
            }
        }
}