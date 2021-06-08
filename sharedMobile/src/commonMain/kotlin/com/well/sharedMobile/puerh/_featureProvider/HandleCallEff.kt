package com.well.sharedMobile.puerh._featureProvider

import com.well.modules.models.User
import com.well.modules.models.WebSocketMsg
import com.well.sharedMobile.puerh._topLevel.*
import com.well.sharedMobile.puerh._topLevel.showSheetThreadSafe
import com.well.sharedMobile.puerh.call.CallEffectHandler
import com.well.sharedMobile.puerh.call.CallFeature
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import com.well.modules.atomic.CloseableFuture
import com.well.modules.atomic.freeze
import com.well.modules.db.users.insertOrReplace
import com.well.modules.utils.permissionsHandler.PermissionsHandler
import com.well.modules.utils.permissionsHandler.requestPermissions
import com.well.modules.utils.puerh.addEffectHandler
import kotlinx.coroutines.launch
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg as TopLevelMsg
import com.well.sharedMobile.puerh.call.CallFeature.Msg as CallMsg
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.Msg as DrawingMsg

internal suspend fun FeatureProvider.handleCallEff(
    eff: CallFeature.Eff,
    listener: (TopLevelMsg) -> Unit
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
            networkManager.send(
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
                    pickSystemImage(listener)
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
        CallFeature.Eff.SystemBack -> {
            appContext.systemBack()
        }
        is CallFeature.Eff.DrawingEff -> {
            when (eff.eff) {
                is DrawingFeature.Eff.NotifyViewSizeUpdate,
                is DrawingFeature.Eff.UploadImage,
                is DrawingFeature.Eff.UploadPaths,
                is DrawingFeature.Eff.NotifyClear,
                is DrawingFeature.Eff.ClearImage,
                -> Unit
                is DrawingFeature.Eff.RequestImageUpdate -> {
                    handleRequestImageUpdate(eff.eff, listener)
                }
            }
        }
    }
}

private fun FeatureProvider.createWebRtcManagerHandler(
    initiateEffect: CallFeature.Eff? = null,
) = CloseableFuture(coroutineScope) {
    feature
        .addEffectHandler(
            CallEffectHandler(
                networkManager,
                webRtcManagerGenerator,
                coroutineScope,
            )
                .apply {
                    initiateEffect?.let { handleEffect(TopLevelFeature.Eff.CallEff(it)) }
                }
        )
}

private suspend fun FeatureProvider.handleRequestImageUpdate(
    eff: DrawingFeature.Eff.RequestImageUpdate,
    listener: (TopLevelMsg) -> Unit
) {
    if (eff.alreadyHasImage) {
        contextHelper.showSheetThreadSafe(
            coroutineScope,
            SuspendAction("Replace image") {
                pickSystemImage(listener)
            },
            SuspendAction("Clear image") {
                listener.invokeDrawingMsg(
                    DrawingMsg.LocalUpdateImage(null)
                )
            },
        )
    } else {
        pickSystemImage(listener)
    }
}

private suspend fun FeatureProvider.pickSystemImage(listener: (TopLevelMsg) -> Unit) {
    val image = contextHelper.pickSystemImageSafe()
    if (image != null) {
        listener.invokeDrawingMsg(
            DrawingMsg.LocalUpdateImage(image)
        )
    }
}

internal fun FeatureProvider.createWebSocketMessageHandler(
    listener: (TopLevelMsg) -> Unit
): (WebSocketMsg) -> Unit = { msg ->
    when (msg) {
        is WebSocketMsg.Back.IncomingCall -> {
            listener.invoke(TopLevelMsg.IncomingCall(msg))
            coroutineScope.launch {
                handleCallPermissions()?.also {
                    listener(TopLevelMsg.EndCall)
                    listener(TopLevelMsg.ShowAlert(it.first.alert))
                }
            }
        }
        is WebSocketMsg.Call.EndCall -> {
            endCall(listener)
        }
        is WebSocketMsg.Back.UpdateUsers -> {
            database.usersQueries.transaction {
                msg.users.forEach(database.usersQueries::insertOrReplace)
            }
        }
        else -> Unit
    }
}

internal suspend fun FeatureProvider.handleCall(
    user: User,
    listener: (TopLevelMsg) -> Unit
) = handleCallPermissions()?.also {
    listener(TopLevelMsg.ShowAlert(it.first.alert))
} ?: listener(TopLevelMsg.StartCall(user))

internal fun FeatureProvider.endCall(
    listener: (TopLevelMsg) -> Unit,
) {
    listener.invoke(TopLevelMsg.EndCall)
    callCloseableContainer.close()
}

private suspend fun FeatureProvider.handleCallPermissions() =
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

private val PermissionsHandler.Type.alert: Alert
    get() = when (this) {
        PermissionsHandler.Type.Camera, PermissionsHandler.Type.Microphone -> Alert.CameraOrMicDenied
    }