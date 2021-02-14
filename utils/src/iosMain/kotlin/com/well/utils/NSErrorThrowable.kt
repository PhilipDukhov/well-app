@file:Suppress("unused")

package com.well.utils

import platform.Foundation.NSError
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

private const val throwableUserInfoKey = "throwableUserInfoKey"

fun NSError.toThrowable(): Throwable =
    (userInfo[throwableUserInfoKey] as? Throwable)
        ?: Throwable(localizedDescription)

fun Throwable.toNSError(): NSError =
    NSError(domain = "com.well.utils", code = 0, userInfo = mapOf(throwableUserInfoKey to this))

fun <T> Continuation<T>.resumeWithException(exception: NSError) =
    resumeWithException(exception.toThrowable())