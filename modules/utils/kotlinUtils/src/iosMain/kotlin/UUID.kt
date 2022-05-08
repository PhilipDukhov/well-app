package com.well.modules.utils.kotlinUtils

import platform.Foundation.NSUUID as NativeUUID
import kotlinx.serialization.Serializable

@Serializable(with = UUIDSerializer::class)
actual class UUID(
    @Suppress("MemberVisibilityCanBePrivate") val nativeUuid: NativeUUID,
) {
    override fun toString(): String = nativeUuid.UUIDString
    actual constructor(uuidString: String) : this(NativeUUID(uUIDString = uuidString))
    actual constructor() : this(NativeUUID())
}