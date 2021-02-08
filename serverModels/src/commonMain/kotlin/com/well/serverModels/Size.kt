package com.well.serverModels

import kotlinx.serialization.Serializable

@Serializable
data class Size(
    val width: Float,
    val height: Float
) {
    constructor(
        width: Int,
        height: Int
    ) : this(width.toFloat(), height.toFloat())

    fun aspectFit(aspectRatio: Size): Size =
        baseAspect(aspectRatio, 1F)

    fun aspectFill(aspectRatio: Size): Size =
        baseAspect(aspectRatio, -1F)

    private fun baseAspect(aspectRatio: Size, ratioMultiplier: Float): Size {
        val mH = height / aspectRatio.height
        val mW = width / aspectRatio.width
        return ((mH - mW) * ratioMultiplier)
            .let { ratio ->
                when {
                    ratio < 0 -> {
                        copy(width = mH * aspectRatio.width)
                    }
                    ratio > 0 -> {
                        copy(height = mW * aspectRatio.height)
                    }
                    else -> {
                        this
                    }
                }
            }
    }
}