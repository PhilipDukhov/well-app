package com.well.modules.features.call.callHandlers

import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.features.call.callFeature.CallEndedReason
import com.well.modules.models.CallInfo
import com.well.modules.models.Notification
import com.well.modules.models.NotificationToken
import com.well.modules.models.WebSocketMsg
import kotlinx.coroutines.CoroutineScope

abstract class CallService : CloseableContainerImpl() {
    data class Services(
        val updateToken: (NotificationToken.ApnsVoip) -> Unit,
        val reportNewCall: (WebSocketMsg.Back.IncomingCall) -> Unit,
        val reportCallCancelled: (Notification.Voip.CanceledCall) -> Unit,
        val answer: () -> Unit,
        val decline: () -> Unit,
        val setMuted: (Boolean) -> Unit,
    )

    abstract suspend fun reportNewIncomingCall(callInfo: CallInfo)
    abstract fun callStartedConnecting()
    abstract fun endCall(reason: CallEndedReason)
    abstract fun reportNewOutgoingCall(callInfo: CallInfo)
    abstract fun reportOutgoingCallConnected()
    abstract fun updateCallHasVideo(hasVideo: Boolean)
}

expect fun createCallService(
    services: CallService.Services,
    parentCoroutineScope: CoroutineScope,
): CallService