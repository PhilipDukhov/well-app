package com.well.utils

import platform.Foundation.NSError
import kotlin.coroutines.Continuation
import kotlin.coroutines.resumeWithException

fun NSError.toThrowable(): Throwable =
    Throwable(localizedDescription)

fun <T> Continuation<T>.resumeWithException(exception: NSError) =
    resumeWithException(exception.toThrowable())