package com.well.sharedMobile.puerh.call

import com.well.modules.models.*
import com.well.sharedMobile.puerh.call.CallFeature.State.Status.*
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.State as DrawingState
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature
import com.well.sharedMobile.puerh.call.drawing.DrawingFeature.copyClear
import com.well.sharedMobile.puerh.call.webRtc.LocalDeviceState
import com.well.sharedMobile.puerh.call.webRtc.RemoteDeviceState
import com.well.modules.utils.*
import com.well.modules.utils.plus
import com.well.modules.utils.toFilterNotNull
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet

object CallFeature {
    fun callingStateAndEffects(user: User) =
        (State(user = user, status = Calling) toSetOf Eff.Initiate(user.id))
            .reduceInitialState()

    fun incomingStateAndEffects(incomingCall: WebSocketMessage.IncomingCall) =
        State(incomingCall, incomingCall.user, Incoming)
            .withEmptySet<State, Eff>()
            .reduceInitialState()

    private fun Pair<State, Set<Eff>>.reduceInitialState(): Pair<State, Set<Eff>> =
        first to (second + Eff.SyncLocalDeviceState(first.localDeviceState))

    private val testDate = Date()
    fun testState(status: State.Status) =
        callingStateAndEffects(
            User(
                id = 1,
                initialized = true,
                fullName = "12",
                profileImageUrl = "https://i.imgur.com/StXm8nf.jpg",
                type = User.Type.Doctor,
            )
        ).first.run {
            copy(
                status = status,
                callStartedDateInfo = State.CallStartedDateInfo(testDate),
                localDeviceState = localDeviceState.copy(cameraEnabled = false),
                localCaptureDimensions = Size(1080, 1920),
                remoteCaptureDimensions = Size(1080, 1920),
            )
        }

    data class State(
        val incomingCall: WebSocketMessage.IncomingCall? = null,
        val user: User,
        val status: Status,
        val localDeviceState: LocalDeviceState = LocalDeviceState.default,
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
        val localVideoView = if (localVideoContext == null) null else VideoView(
            context = localVideoContext,
            hidden = !localCameraEnabled || viewPoint == ViewPoint.Partner,
            position = when (viewPoint) {
                ViewPoint.Partner,
                ViewPoint.Both,
                -> VideoView.Position.Minimized
                ViewPoint.Mine -> VideoView.Position.FullScreen
            }
        )
        val remoteVideoView = if (remoteVideoContext == null) null else VideoView(
            context = remoteVideoContext,
            hidden = !remoteCameraEnabled || viewPoint == ViewPoint.Mine,
            position = VideoView.Position.FullScreen
        )

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

//        fun testIncStatus() = copy(status = status.nextEnumValue())
    }

    sealed class Msg {
        object Accept : Msg()
        object End : Msg()
        object Back : Msg()
        object DataConnectionEstablished : Msg()
        data class UpdateLocalCaptureDimensions(val dimensions: Size) : Msg()
        data class UpdateRemoteCaptureDimensions(val dimensions: Size) : Msg()
        data class UpdateStatus(val status: State.Status) : Msg()
        data class UpdateLocalVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteVideoContext(val viewContext: VideoViewContext) : Msg()
        data class UpdateRemoteDeviceState(val deviceState: RemoteDeviceState) : Msg()
        data class SetMicEnabled(val enabled: Boolean) : Msg()
        data class SetCameraEnabled(val enabled: Boolean) : Msg()
        data class SetAudioSpeakerEnabled(val enabled: Boolean) : Msg()
        data class SetIsFrontCamera(val isFrontCamera: Boolean) : Msg()
        object InitializeDrawing : Msg()
        data class LocalUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        data class RemoteUpdateViewPoint(val viewPoint: State.ViewPoint) : Msg()
        data class UpdateControlSet(val controlSet: State.ControlSet) : Msg()
        data class DrawingMsg(val msg: DrawingFeature.Msg) : Msg()
    }

    sealed class Eff {
        data class Initiate(val userId: UserId) : Eff()
        data class Accept(val incomingCall: WebSocketMessage.IncomingCall) : Eff()
        object End : Eff()
        object ChooseViewPoint : Eff()
        object SystemBack : Eff()
        data class SyncLocalDeviceState(val localDeviceState: LocalDeviceState) : Eff()
        data class NotifyLocalCaptureDimensionsChanged(val dimensions: Size) : Eff()
        data class NotifyDeviceStateChanged(val deviceState: RemoteDeviceState) : Eff()
        data class NotifyUpdateViewPoint(val viewPoint: State.ViewPoint) : Eff()
        data class DrawingEff(val eff: DrawingFeature.Eff) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.Accept -> state.incomingCall?.let { incomingCall ->
            state.copy(status = Connecting) to setOf(Eff.Accept(incomingCall))
        } ?: throw IllegalStateException("$msg | $state")
        is Msg.End -> state toSetOf Eff.End
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

    private fun State.reduceInitializeDrawing() = when {
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

    private fun State.reduceRemoteUpdateViewPoint(viewPoint: State.ViewPoint) =
        copy(
            viewPoint = viewPoint,
            controlSetSaved = if (viewPoint == State.ViewPoint.Both)
                State.ControlSet.Call
            else
                controlSetSaved,
            drawingState = drawingStateCopyViewPoint(viewPoint),
        ).reduceUpdateLocalDeviceState(forceMineBackCamera = true)

    private fun State.reduceLocalUpdateViewPoint(viewPoint: State.ViewPoint) =
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
        modifier: LocalDeviceState.() -> LocalDeviceState
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
            if (newState != localDeviceState) Eff.NotifyDeviceStateChanged(RemoteDeviceState(newState)) else null,
        ).filterNotNull().toSet()
    }
}

