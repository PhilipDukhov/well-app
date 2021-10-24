package com.well.sharedMobile.featureProvider

import com.well.modules.atomic.CloseableFuture
import com.well.modules.atomic.freeze
import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.permissionsHandler.requestPermissions
import com.well.modules.utils.puerh.addEffectHandler
import com.well.modules.viewHelpers.Alert
import com.well.modules.viewHelpers.SuspendAction
import com.well.sharedMobile.TopLevelFeature
import com.well.modules.viewHelpers.pickSystemImageSafe
import com.well.modules.viewHelpers.showSheetThreadSafe
import com.well.modules.features.call.callHandlers.CallEffectHandler
import com.well.modules.features.call.callFeature.CallFeature
import com.well.modules.features.call.callFeature.drawing.DrawingFeature
import com.well.modules.utils.puerh.adapt
import com.well.modules.utils.sharedImage.ImageContainer
import com.well.sharedMobile.TopLevelFeature.Msg as TopLevelMsg
import com.well.modules.features.call.callFeature.CallFeature.Msg as CallMsg
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.Msg as DrawingMsg
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

internal suspend fun FeatureProviderImpl.handleCallEff(
    eff: CallFeature.Eff,
    listener: (TopLevelMsg) -> Unit,
) {
    when (eff) {
        is CallFeature.Eff.NotifyDeviceStateChanged,
        is CallFeature.Eff.SyncLocalDeviceState,
        is CallFeature.Eff.NotifyUpdateViewPoint,
        is CallFeature.Eff.NotifyLocalCaptureDimensionsChanged,
        CallFeature.Eff.SystemBack,
        -> Unit
        is CallFeature.Eff.Initiate, is CallFeature.Eff.Accept -> {
            callCloseableContainer.addCloseableChild(
                createWebRtcManagerHandler(eff).freeze()
            )
        }
        is CallFeature.Eff.End -> {
            networkManager.sendCall(
                WebSocketMsg.Call.EndCall(WebSocketMsg.Call.EndCall.Reason.Decline)
            )
            endCall(listener)
        }
        CallFeature.Eff.ChooseViewPoint -> {
            fun startDrawing(viewPoint: CallFeature.State.ViewPoint) =
                listener(
                    TopLevelMsg.CallMsg(
                        CallMsg.LocalUpdateViewPoint(
                            viewPoint
                        )
                    )
                )
            contextHelper.showSheetThreadSafe(
                coroutineScope,
                SuspendAction("Draw on image") {
                    val image = contextHelper.pickSystemImageSafe()
                    if (image != null) {
                        listener.invokeDrawingMsg(
                            DrawingMsg.LocalUpdateImage(image.toImageContainer())
                        )
                    }
                },
                SuspendAction("Draw on your own camera") {
                    startDrawing(
                        CallFeature.State.ViewPoint.Mine
                    )
                },
                SuspendAction("Draw on your partners camera") {
                    startDrawing(
                        CallFeature.State.ViewPoint.Partner
                    )
                },
            )
        }
        is CallFeature.Eff.DrawingEff -> Unit
    }
}

private fun FeatureProviderImpl.createWebRtcManagerHandler(
    initiateEffect: CallFeature.Eff? = null,
) = CloseableFuture(coroutineScope) {
    addEffectHandler(
        CallEffectHandler(
            CallEffectHandler.Services(
                isConnectedFlow = networkManager.isConnectedFlow,
                callWebSocketMsgFlow = networkManager.webSocketMsgSharedFlow.filterIsInstance(),
                sendCallWebSocketMsg = networkManager::sendCall,
                sendFrontWebSocketMsg = networkManager::sendFront,
                requestImageUpdate = { eff, updateImage ->
                    coroutineScope.launch {
                        handleRequestImageUpdate(eff, updateImage)
                    }
                },
            ),
            webRtcManagerGenerator,
            coroutineScope,
        ).apply {
            initiateEffect?.let { handleEffect(it) }
        }.adapt(
            effAdapter = { (it as? TopLevelFeature.Eff.CallEff)?.eff },
            msgAdapter = { TopLevelMsg.CallMsg(it) }
        )
    )
}

private suspend fun FeatureProviderImpl.handleRequestImageUpdate(
    eff: DrawingFeature.Eff.RequestImageUpdate,
    updateImage: (ImageContainer?) -> Unit,
) {
    if (eff.alreadyHasImage) {
        contextHelper.showSheetThreadSafe(
            coroutineScope,
            SuspendAction("Replace image") {
                contextHelper.pickSystemImageSafe()?.let { updateImage(it.toImageContainer()) }
            },
            SuspendAction("Clear image") {
                updateImage(null)
            },
        )
    } else {
        contextHelper.pickSystemImageSafe()?.let { updateImage(it.toImageContainer()) }
    }
}

internal suspend fun FeatureProviderImpl.handleCall(
    user: User,
    listener: (TopLevelMsg) -> Unit,
) = handleCallPermissions()?.also {
    listener(TopLevelMsg.ShowAlert(it.first.alert))
} ?: listener(TopLevelMsg.StartCall(user))

internal fun FeatureProviderImpl.endCall(
    listener: (TopLevelMsg) -> Unit,
) {
    listener.invoke(TopLevelMsg.EndCall)
    callCloseableContainer.close()
}

internal suspend fun FeatureProviderImpl.handleCallPermissions() =
    permissionsHandler
        .requestPermissions(
            PermissionsHandler.Type.Camera,
            PermissionsHandler.Type.Microphone,
        )
        .firstOrNull {
            it.second != PermissionsHandler.Result.Authorized
        }

private fun ((TopLevelMsg) -> Unit).invokeDrawingMsg(msg: DrawingMsg) =
    invoke(
        TopLevelMsg.CallMsg(
            CallMsg.DrawingMsg(
                msg
            )
        )
    )

internal val PermissionsHandler.Type.alert: Alert
    get() = when (this) {
        PermissionsHandler.Type.Camera, PermissionsHandler.Type.Microphone -> Alert.CameraOrMicDenied
    }