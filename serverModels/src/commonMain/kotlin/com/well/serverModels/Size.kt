package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Size(val width: Double, val height: Double) {
    constructor(width: Int, height: Int) : this(width.toDouble(), height.toDouble())
}