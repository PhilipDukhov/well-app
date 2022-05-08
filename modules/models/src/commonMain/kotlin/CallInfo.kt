package com.well.modules.models

import com.well.modules.utils.kotlinUtils.UUID

interface CallInfo {
    val id: UUID
    val hasVideo: Boolean
    val senderName: String
}