package com.well.modules.utils.kotlinUtils

import kotlinx.serialization.Serializable

@Serializable(with = UUIDSerializer::class)
expect class UUID() {
    constructor(uuidString: String)
}