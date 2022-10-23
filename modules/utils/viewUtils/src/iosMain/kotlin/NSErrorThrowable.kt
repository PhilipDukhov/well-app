package com.well.modules.utils.viewUtils

import platform.Foundation.NSError
import platform.Foundation.NSErrorDomain
import platform.darwin.NSInteger
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val exceptionUserInfoKey = "exceptionUserInfoKey"

fun NSError.toException(): Exception =
    (userInfo[exceptionUserInfoKey] as? Exception)
        ?: Exception(localizedDescription)

fun NSError.getCodeNameString(domain: NSErrorDomain, codes: List<kotlin.reflect.KProperty0<NSInteger>>): String? =
    takeIf { it.domain == domain }
        ?.let {
            codes
                .firstOrNull { it.get() == code }
                ?.name
        }

// used from iOS part
@Suppress("unused")
fun Exception.toNSError(): NSError =
    NSError(domain = "com.well.modules.utils", code = 0, userInfo = mapOf(exceptionUserInfoKey to this))

fun <T> Continuation<T>.resumeWithError(error: NSError) =
    resumeWithException(error.toException())

fun Continuation<Unit>.resumeWithOptionalError(error: NSError?) =
    if (error != null) {
        resumeWithError(error)
    } else {
        resume(Unit)
    }
