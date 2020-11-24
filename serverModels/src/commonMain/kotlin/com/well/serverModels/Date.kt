package com.well.serverModels

import kotlinx.serialization.Serializable
import com.well.serverModels.serializers.DateSerializer

@Serializable(with = DateSerializer::class)
expect class Date(millis: Long) {
    val millis: Long
}