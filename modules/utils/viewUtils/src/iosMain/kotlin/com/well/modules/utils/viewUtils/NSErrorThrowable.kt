@file:Suppress("unused")

package com.well.modules.utils.viewUtils

import platform.Foundation.NSError
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

private const val throwableUserInfoKey = "throwableUserInfoKey"

fun NSError.toThrowable(): Throwable =
    (userInfo[throwableUserInfoKey] as? Throwable)
        ?: Throwable(localizedDescription)

fun Throwable.toNSError(): NSError =
    NSError(domain = "com.well.modules.utils", code = 0, userInfo = mapOf(throwableUserInfoKey to this))

fun <T> Continuation<T>.resumeWithError(error: NSError) =
    resumeWithException(error.toThrowable())