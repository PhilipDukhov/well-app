package com.well.modules.features.call.callFeature

import com.well.modules.features.call.callFeature.CallFeature.State.Status.Calling
import com.well.modules.features.call.callFeature.CallFeature.State.Status.Connecting
import com.well.modules.features.call.callFeature.CallFeature.State.Status.Incoming
import com.well.modules.features.call.callFeature.CallFeature.State.Status.Ongoing
import com.well.modules.features.call.callFeature.drawing.DrawingFeature
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.copyClear
import com.well.modules.features.call.callFeature.webRtc.LocalDeviceState
import com.well.modules.features.call.callFeature.webRtc.RemoteDeviceState
import com.well.modules.models.CallId
import com.well.modules.models.CallInfo
import com.well.modules.models.Size
import com.well.modules.models.User
import com.well.modules.models.date.Date
import com.well.modules.models.date.secondsSinceNow
import com.well.modules.puerhBase.plus
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.kotlinUtils.toFilterNotNull
import com.well.modules.utils.viewUtils.nativeFormat
import com.well.modules.features.call.callFeature.drawing.DrawingFeature.State as DrawingState

object CallFeature {
    fun callingStateAndEffects(user: User, hasVideo: Boolean) =
        (State(
            user = user,
            status = Calling,
            callId = CallId.new()
        ) toSetOf Eff.Initiate(user, hasVideo)
                ).reduceInitialState()

    fun incomingStateAndEffects(incomingCall: CallInfo) =
        State(
            incomingCall = incomingCall,
            callId = incomingCall.id,
            user = incomingCall.user,
            status = Incoming
        )
            .withEmptySet<State, Eff>()
            .reduceInitialState()

    private fun Pair<State, Set<Eff>>.reduceInitialState(): Pair<State, Set<Eff>> =
        first to (second + Eff.SyncLocalDeviceState(first.localDeviceState))

    data class State(
        val incomingCall: CallInfo? = null,
        val callId: CallId,
        val user: User,
        val status: Status,
        val localDeviceState: LocalDeviceState = LocalDeviceState.default(cameraEnabled = incomingCall?.hasVideo),
        val remoteDeviceState: RemoteDeviceState? = null,
        val callStartedDateInfo: CallStartedDateInfo? = null,
        val viewPoint: ViewPoint = ViewPoint.Both,
        val drawingState: DrawingState = DrawingState(),
        internal val controlSetSaved: ControlSet = ControlSet.Call,
        internal val localVideoContext: VideoViewContext? = null,
        internal val localCaptureDimensions: Size? = null,
        internal val remoteVideoContext: VideoViewContext? = null,
        internal val remoteCaptureDimensions: Size? = null,
    ) {
        val controlSet = if (drawingState.image != null) ControlSet.Drawing else controlSetSaved
        internal val localCameraEnabled = localDeviceState.cameraEnabled
        internal val remoteCameraEnabled = remoteDeviceState?.cameraEnabled != false
        val localVideoView = localVideoContext?.let {
            VideoView(
                context = localVideoContext,
                hidden = !localCameraEnabled || viewPoint == ViewPoint.Partner,
                position = when (viewPoint) {
                    ViewPoint.Partner,
                    ViewPoint.Both,
                    -> VideoView.Position.Minimized
                    ViewPoint.Mine -> VideoView.Position.FullScreen
                }
            )
        }
        val remoteVideoView = remoteVideoContext?.let {
            VideoView(
                context = remoteVideoContext,
                hidden = !remoteCameraEnabled || viewPoint == ViewPoint.Mine,
                position = VideoView.Position.FullScreen
            )
        }

        data class CallStartedDateInfo(val date: Date) {
            val secondsPassedFormatted: String
                get() = date.secondsSinceNow().toInt().let {
                    String.nativeFormat("%02d:%02d", it / 60, it % 60)
                }
        }

        enum class Status {
            Calling,
            Incoming,
            Connecting,
            Ongoing,
            ;

            val stringRepresentation: String
                get() = when (this) {
                    Calling -> "Calling..."
                    Incoming -> "Incoming"
                    Connecting -> "Connecting..."
                    Ongoing -> "Ongoing"
                }
        }

        data class VideoView(
            val context: VideoViewContext,
            val hidden: Boolean,
            val position: Position,
        ) {
            enum class Position {
                FullScreen,
                Minimized,
                ;
            }
        }

        enum class ViewPoint {
            Both,
            Mine,
            Partner,
            ;
        }

        enum class ControlSet {
            Call,
            Drawing,
            ;
        }
    }

    sealed class Msg {
        object Accept : Msg()
        object End : Msg()
        object Back : Msg()
        object DataConnectionEstablished : Msg()
        class UpdateLocalCaptureDimensions(val dimensions: Size) : Msg()
        class UpdateRemoteCaptureDimensions(val dimensions: Size) : Msg()
        class UpdateStatus(val status: State.Status) : Msg()
        class UpdateLocalVideoContext(val viewContext: VideoViewContext) : Msg()
        class UpdateRemoteVideoContext(val viewContext: VideoViewContext) : Msg()
        class UpdateRemoteDeviceState(val deviceState: RemoteDeviceState) : Msg()
        class SetMicEnabled(val enabled: Boolean) : Msg()
        class SetCameraEnabled(val enabled: Boolean) : Msg()
        class SetAudioSpeakerEnabled(val enabled: Boolean) : Msg()
        class SetIsFrontCamera(val isFrontCamera: Boolean) : Msg()
        object InitializeDrawing : Msg()
        class LocalUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        class RemoteUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        class UpdateControlSet(val controlSet: State.ControlSet) : Msg()
        class DrawingMsg(val msg: DrawingFeature.Msg) : Msg()
    }

    sealed interface Eff {
        class Initiate(val user: User, val hasVideo: Boolean) : Eff
        class Accept(val incomingCall: CallInfo) : Eff
        class End(val reason: CallEndedReason) : Eff
        object ChooseViewPoint : Eff
        object SystemBack : Eff
        class SyncLocalDeviceState(val localDeviceState: LocalDeviceState) : Eff
        class NotifyLocalCaptureDimensionsChanged(val dimensions: Size) : Eff
        class NotifyDeviceStateChanged(val deviceState: RemoteDeviceState) : Eff
        class NotifyUpdateViewPoint(val viewPoint: State.ViewPoint) : Eff
        class DrawingEff(val eff: DrawingFeature.Eff) : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Accept -> state.incomingCall?.let { incomingCall ->
            state.copy(status = Connecting) to setOf(Eff.Accept(incomingCall))
        } ?: throw IllegalStateException("$msg | $state")
        is Msg.End -> state toSetOf Eff.End(if (state.status == State.Status.Ongoing) CallEndedReason.Finished else CallEndedReason.Failed)
        is Msg.Back -> {
            when (state.controlSet) {
                State.ControlSet.Call -> {
                    state toSetOf Eff.SystemBack
                }
                State.ControlSet.Drawing -> {
                    if (state.drawingState.image != null) {
                        state.reduceDrawingMsg(DrawingFeature.Msg.LocalUpdateImage(null))
                    } else {
                        state.copyUpdateControlSet(State.ControlSet.Call).withEmptySet()
                    }
                }
            }
        }
        is Msg.UpdateLocalCaptureDimensions -> {
            state.copy(localCaptureDimensions = msg.dimensions) toSetOf
                    Eff.NotifyLocalCaptureDimensionsChanged(msg.dimensions)
        }
        is Msg.UpdateRemoteCaptureDimensions -> {
            state.copy(remoteCaptureDimensions = msg.dimensions).withEmptySet()
        }
        is Msg.UpdateLocalVideoContext -> {
            state.copy(localVideoContext = msg.viewContext).withEmptySet()
        }
        is Msg.UpdateRemoteVideoContext -> {
            state.copy(remoteVideoContext = msg.viewContext).withEmptySet()
        }
        is Msg.UpdateStatus -> {
            state.copy(status = msg.status).withEmptySet()
        }
        is Msg.SetMicEnabled -> {
            state.reduceCopyDeviceState { copy(micEnabled = msg.enabled) }
        }
        is Msg.SetCameraEnabled -> {
            state.reduceCopyDeviceState { copy(cameraEnabled = msg.enabled) }
        }
        is Msg.SetAudioSpeakerEnabled -> {
            state.reduceCopyDeviceState { copy(audioSpeakerEnabled = msg.enabled) }
        }
        is Msg.SetIsFrontCamera -> {
            state.reduceCopyDeviceState { copy(isFrontCamera = msg.isFrontCamera) }
        }
        is Msg.InitializeDrawing -> {
            state.reduceInitializeDrawing()
        }
        is Msg.LocalUpdateViewPoint -> {
            state.reduceLocalUpdateViewPoint(msg.viewPoint)
        }
        is Msg.RemoteUpdateViewPoint -> {
            state.reduceRemoteUpdateViewPoint(msg.viewPoint)
        }
        is Msg.UpdateControlSet -> {
            state.copyUpdateControlSet(msg.controlSet).withEmptySet()
        }
        is Msg.UpdateRemoteDeviceState -> {
            state.copy(remoteDeviceState = msg.deviceState).withEmptySet()
        }
        is Msg.DataConnectionEstablished -> {
            state.copy(
                status = Ongoing,
                callStartedDateInfo = state.callStartedDateInfo ?: State.CallStartedDateInfo(Date())
            ) toFilterNotNull setOf(
                state.localCaptureDimensions?.let(Eff::NotifyLocalCaptureDimensionsChanged),
                state.drawingState.notifyViewSizeUpdateEff()?.let(Eff::DrawingEff)
            )
        }
        is Msg.DrawingMsg -> {
            state.reduceDrawingMsg(msg.msg)
        }
    }

    private fun State.reduceInitializeDrawing(): Pair<State, Set<Eff>> = when {
        viewPoint != State.ViewPoint.Both -> {
            copy(
                controlSetSaved = State.ControlSet.Drawing
            ).withEmptySet()
        }
        localCameraEnabled && remoteCameraEnabled -> {
            this toSetOf Eff.ChooseViewPoint
        }
        localCameraEnabled -> {
            this.reduceLocalUpdateViewPoint(State.ViewPoint.Mine)
        }
        remoteCameraEnabled -> {
            this.reduceLocalUpdateViewPoint(State.ViewPoint.Partner)
        }
        else -> {
            throw IllegalStateException("reduceInitializeDrawing $this")
        }
    }

    private fun State.reduceRemoteUpdateViewPoint(viewPoint: State.ViewPoint): Pair<State, Set<Eff>> =
        copy(
            viewPoint = viewPoint,
            controlSetSaved = if (viewPoint == State.ViewPoint.Both)
                State.ControlSet.Call
            else
                controlSetSaved,
            drawingState = drawingStateCopyViewPoint(viewPoint),
        ).reduceUpdateLocalDeviceState(forceMineBackCamera = true)

    private fun State.reduceLocalUpdateViewPoint(viewPoint: State.ViewPoint): Pair<State, Set<Eff>> =
        copy(
            viewPoint = viewPoint,
            controlSetSaved = if (viewPoint == State.ViewPoint.Both)
                State.ControlSet.Call
            else
                State.ControlSet.Drawing,
            drawingState = drawingStateCopyViewPoint(viewPoint),
        ).reduceUpdateLocalDeviceState(forceMineBackCamera = true) plus Eff.NotifyUpdateViewPoint(
            viewPoint
        )

    private fun State.drawingStateCopyViewPoint(viewPoint: State.ViewPoint) =
        drawingState.copy(
            videoAspectRatio = when (viewPoint) {
                State.ViewPoint.Both -> null
                State.ViewPoint.Mine -> localCaptureDimensions
                State.ViewPoint.Partner -> remoteCaptureDimensions
            },
        ).copyClear(saveHistory = false)

    private fun State.reduceDrawingMsg(msg: DrawingFeature.Msg): Pair<State, Set<Eff>> {
        val (state, effs) = DrawingFeature.reducer(msg, drawingState)
        return copy(
            drawingState = state,
        ) to effs.mapTo(HashSet(), Eff::DrawingEff)
    }

    private fun State.copyUpdateControlSet(controlSet: State.ControlSet) =
        copy(
            controlSetSaved = controlSet
        )

    private fun State.reduceCopyDeviceState(
        modifier: LocalDeviceState.() -> LocalDeviceState,
    ) = reduceUpdateLocalDeviceState(modifier(localDeviceState))

    private fun State.reduceUpdateLocalDeviceState(
        state: LocalDeviceState = localDeviceState,
        forceMineBackCamera: Boolean = false,
    ): Pair<State, Set<Eff>> {
        val newState = if (forceMineBackCamera && viewPoint == State.ViewPoint.Mine)
            state.copy(isFrontCamera = false)
        else
            state
        return copy(localDeviceState = newState) to setOf(
            Eff.SyncLocalDeviceState(
                if (viewPoint == State.ViewPoint.Partner)
                    newState.copy(cameraEnabled = false)
                else
                    newState
            ),
            if (newState != localDeviceState) Eff.NotifyDeviceStateChanged(
                RemoteDeviceState(
                    newState
                )
            ) else null,
        ).filterNotNull().toSet()
    }
}