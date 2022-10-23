package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.atomic.CloseableFuture
import com.well.modules.atomic.freeze
import com.well.modules.features.call.callFeature.CallEndedReason
import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.features.call.callFeature.drawing.DrawingFeature
import com.well.modules.features.call.callHandlers.CallEffectHandler
import com.well.modules.features.topLevel.topLevelFeature.FeatureEff
import com.well.modules.features.topLevel.topLevelFeature.FeatureMsg
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature
import com.well.modules.models.CallInfo
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.puerhBase.adapt
import com.well.modules.puerhBase.addEffectHandler
import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.SuspendAction
import com.well.modules.utils.viewUtils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.viewUtils.permissionsHandler.requestPermissions
import com.well.modules.utils.viewUtils.pickSystemImageSafe
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import com.well.modules.utils.viewUtils.showSheetThreadSafe
import com.well.modules.features.call.callFeature.CallFeature.Msg as CallMsg
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.Msg as DrawingMsg
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Msg as TopLevelMsg
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

internal suspend fun TopLevelFeatureProviderImpl.handleCallEff(
    eff: CallFeature.Eff,
    listener: (TopLevelMsg) -> Unit,
    position: TopLevelFeature.State.ScreenPosition,
) {
    when (eff) {
        is CallFeature.Eff.NotifyDeviceStateChanged,
        is CallFeature.Eff.SyncLocalDeviceState,
        is CallFeature.Eff.NotifyUpdateViewPoint,
        is CallFeature.Eff.NotifyLocalCaptureDimensionsChanged,
        is CallFeature.Eff.SystemBack,
        is CallFeature.Eff.DrawingEff,
        -> Unit
        is CallFeature.Eff.Initiate, is CallFeature.Eff.Accept -> {
            callCloseableContainer.addCloseableChild(
                createWebRtcManagerHandler(eff, position).freeze()
            )
        }
        is CallFeature.Eff.End -> {
            networkManager.sendCall(
                WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Decline)
            )
            endCall(listener, eff.reason)
        }
        CallFeature.Eff.ChooseViewPoint -> {
            fun startDrawing(viewPoint: CallFeature.State.ViewPoint) =
                listener(
                    FeatureMsg.Call(
                        CallMsg.LocalUpdateViewPoint(viewPoint),
                        position = position,
                    )
                )
            systemHelper?.showSheetThreadSafe(
                SuspendAction("Draw on image") {
                    val image = systemHelper?.pickSystemImageSafe()
                    if (image != null) {
                        listener.invokeDrawingMsg(
                            DrawingMsg.LocalUpdateImage(image.toImageContainer()),
                            position,
                        )
                    }
                },
                SuspendAction("Draw on your own camera") {
                    startDrawing(CallFeature.State.ViewPoint.Mine)
                },
                SuspendAction("Draw on your partners camera") {
                    startDrawing(CallFeature.State.ViewPoint.Partner)
                },
            )
        }
    }
}

private fun TopLevelFeatureProviderImpl.createWebRtcManagerHandler(
    initiateEffect: CallFeature.Eff? = null,
    position: TopLevelFeature.State.ScreenPosition,
) = CloseableFuture(coroutineScope) {
    addEffectHandler(
        CallEffectHandler(
            CallEffectHandler.Services(
                callService = callService,
                isConnectedFlow = networkManager.isConnectedFlow,
                callWebSocketMsgFlow = networkManager.webSocketMsgSharedFlow.filterIsInstance(),
                sendCallWebSocketMsg = networkManager::sendCall,
                sendFrontWebSocketMsg = networkManager::sendFront,
                requestImageUpdate = { eff, updateImage ->
                    coroutineScope.launch {
                        handleRequestImageUpdate(eff, updateImage)
                    }
                },
                onStartOutgoingCall = {
                    callService?.reportNewOutgoingCall(it)
                },
                onStartedConnecting = {
                    callService?.callStartedConnecting()
                },
                onConnected = {},
            ),
            webRtcManagerGenerator,
            coroutineScope,
        ).apply {
            initiateEffect?.let { handleEffect(it) }
        }.adapt(
            effAdapter = { (it as? FeatureEff.Call)?.eff },
            msgAdapter = { FeatureMsg.Call(it, position) }
        )
    )
}

internal suspend fun TopLevelFeatureProviderImpl.handleIncomingCall(
    callInfo: CallInfo,
    listener: (TopLevelMsg) -> Unit,
) {
    listener.invoke(TopLevelMsg.IncomingCall(callInfo))
    val failedPermissions = handleCallPermissions()
    if (failedPermissions == null) {
        callService?.reportNewIncomingCall(callInfo)
    } else {
        listener(TopLevelMsg.EndCall)
        listener(TopLevelMsg.ShowAlert(failedPermissions.first.alert))
    }
}

private suspend fun TopLevelFeatureProviderImpl.handleRequestImageUpdate(
    eff: DrawingFeature.Eff.RequestImageUpdate,
    updateImage: (ImageContainer?) -> Unit,
) {
    if (eff.alreadyHasImage) {
        systemHelper?.showSheetThreadSafe(
            SuspendAction("Replace image") {
                systemHelper?.pickSystemImageSafe()?.let { updateImage(it.toImageContainer()) }
            },
            SuspendAction("Clear image") {
                updateImage(null)
            },
        )
    } else {
        systemHelper?.pickSystemImageSafe()?.let { updateImage(it.toImageContainer()) }
    }
}

internal suspend fun TopLevelFeatureProviderImpl.handleCall(
    user: User,
    hasVideo: Boolean,
    listener: (TopLevelMsg) -> Unit,
) = handleCallPermissions()?.also {
    listener(TopLevelMsg.ShowAlert(it.first.alert))
} ?: listener(TopLevelMsg.StartCall(user, hasVideo))

internal fun TopLevelFeatureProviderImpl.endCall(
    listener: (TopLevelMsg) -> Unit,
    reason: CallEndedReason,
) {
    listener.invoke(TopLevelMsg.EndCall)
    callCloseableContainer.close()
    callService?.endCall(reason)
}

internal suspend fun TopLevelFeatureProviderImpl.handleCallPermissions() =
    permissionsHandler
        ?.requestPermissions(
            PermissionsHandler.Type.Camera,
            PermissionsHandler.Type.Microphone,
        )
        ?.firstOrNull {
            it.second != PermissionsHandler.Result.Authorized
        }

private fun ((TopLevelMsg) -> Unit).invokeDrawingMsg(
    msg: DrawingMsg,
    position: TopLevelFeature.State.ScreenPosition,
) = invoke(
    FeatureMsg.Call(
        CallMsg.DrawingMsg(
            msg
        ),
        position
    )
)

internal val PermissionsHandler.Type.alert: Alert
    get() = when (this) {
        PermissionsHandler.Type.Camera -> Alert.CameraDenied
        PermissionsHandler.Type.Microphone -> Alert.MicDenied
        PermissionsHandler.Type.CallPhone -> Alert.CallDenied
    }