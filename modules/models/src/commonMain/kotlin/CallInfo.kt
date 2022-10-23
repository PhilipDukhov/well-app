package com.well.modules.models

import com.well.modules.utils.kotlinUtils.UUID
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
@JvmInline
value class CallId(val uuid: UUID) {
    companion object {
        fun new() = CallId(UUID())
    }
}

interface CallInfo {
    val id: CallId
    val hasVideo: Boolean
    val user: User
}