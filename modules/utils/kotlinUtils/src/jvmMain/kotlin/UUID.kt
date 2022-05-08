package com.well.modules.utils.kotlinUtils

import java.util.UUID as NativeUUID
import kotlinx.serialization.Serializable

@Serializable(with = UUIDSerializer::class)
actual class UUID(
    @Suppress("MemberVisibilityCanBePrivate") val nativeUuid: NativeUUID,
) {
    override fun toString(): String = nativeUuid.toString()
    actual constructor(uuidString: String) : this(NativeUUID.fromString(uuidString))
    actual constructor() : this(NativeUUID.randomUUID())
}