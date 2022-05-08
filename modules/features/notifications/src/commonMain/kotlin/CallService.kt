package com.well.modules.features.notifications

import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.models.CallInfo
import com.well.modules.models.Notification
import kotlinx.coroutines.CoroutineScope

internal expect class CallService(
    services: CallServiceServices,
    parentCoroutineScope: CoroutineScope,
): CloseableContainerImpl {
    suspend fun reportNewIncomingCall(callInfo: CallInfo)
    fun updateCallHasVideo(hasVideo: Boolean)
    suspend fun reportNewOutgoingCall(callInfo: CallInfo)
    fun reportOutgoingCallConnected()
}

data class CallServiceServices(
    val updateToken: (String?) -> Unit,
    val reportNewCall: (Notification.IncomingCall) -> Unit,
    val answer: () -> Unit,
    val decline: () -> Unit,
    val setMuted: (Boolean) -> Unit,
)