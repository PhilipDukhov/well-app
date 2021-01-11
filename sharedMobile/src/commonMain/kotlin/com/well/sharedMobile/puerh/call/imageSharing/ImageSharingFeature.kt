package com.well.sharedMobile.puerh.call.imageSharing

import com.well.serverModels.Color
import com.well.serverModels.Path
import com.well.serverModels.Point
import com.well.serverModels.Size
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object ImageSharingFeature {
    fun initialState(role: State.Role) =
        State(role) to mutableSetOf<Eff>(Eff.Init)
            .apply {
                if (role == State.Role.Editor) {
                    this += Eff.RequestImageUpdate
                }
            }

    data class State(
        val role: Role,
        val localViewSize: Size? = null,
        val remoteViewSize: Size? = null,
        val image: ImageContainer? = null,
        val currentColor: Color = Color.red,
        val paths: List<Path> = listOf(),
    ) {
        enum class Role {
            Editor,
            Viewer,
        }
    }

    sealed class Msg {
        object SwitchToViewer : Msg()
        data class UpdateLocalViewSize(val size: Size) : Msg()
        data class UpdateRemoteViewSize(val size: Size) : Msg()
        object ImageUpdateCancelled : Msg()
        data class LocalUpdateImage(val image: ImageContainer) : Msg()
        data class RemoteUpdateImage(val image: ImageContainer) : Msg()
        data class UpdateColor(val color: Color) : Msg()
        object StartPath : Msg()
        data class AddPoint(val point: Point) : Msg()
        data class UpdatePaths(val paths: List<Path>) : Msg()
        object Close : Msg()
    }

    sealed class Eff {
        object Init : Eff()
        object RequestImageUpdate : Eff()
        object Close : Eff()
        data class NotifyViewSizeUpdate(val size: Size) : Eff()
        data class UploadImage(
            val image: ImageContainer,
            val remoteViewSize: Size
        ) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        Msg.SwitchToViewer -> {
            state.copy(role = State.Role.Viewer)
                .withEmptySet()
        }
        is Msg.UpdateLocalViewSize -> {
            state.reduceLocalViewSizeUpdate(msg.size)
        }
        is Msg.UpdateRemoteViewSize -> {
            state.copy(remoteViewSize = msg.size)
                .reduceSendImageUpdate()
        }
        Msg.ImageUpdateCancelled -> {
            state toSetOf if (state.image != null) {
                null
            } else {
                Eff.Close
            }
        }
        is Msg.LocalUpdateImage -> {
            state.copy(image = msg.image)
                .reduceSendImageUpdate()
        }
        is Msg.RemoteUpdateImage -> {
            state.copy(image = msg.image)
                .withEmptySet()
        }
        is Msg.UpdateColor -> {
            state.copy(currentColor = msg.color)
                .withEmptySet()
        }
        Msg.StartPath -> {
            state.copy(paths = state.paths + Path(listOf(), state.currentColor))
                .withEmptySet()
        }
        is Msg.AddPoint -> {
            state.copyAddPoint(msg.point)
                .withEmptySet()
        }
        is Msg.UpdatePaths -> {
            state.copy(paths = msg.paths)
                .withEmptySet()
        }
        Msg.Close -> {
            state toSetOf Eff.Close
        }
    }

    private fun State.reduceLocalViewSizeUpdate(size: Size) =
        if (localViewSize != size) {
            copy(localViewSize = size) toSetOf Eff.NotifyViewSizeUpdate(size)
        } else {
            this.withEmptySet()
        }

    private fun State.reduceSendImageUpdate() =
        this toSetOf if (role == State.Role.Editor && image != null && remoteViewSize != null) {
            Eff.UploadImage(image, remoteViewSize)
        } else {
            null
        }

    private fun State.copyAddPoint(point: Point): State {
        val currentPath = paths.last()
        val paths = paths.toMutableList()
//        if (currentPath.points.count() > 100) {
//            paths.add(
//                currentPath.copy(
//                    points = listOf(
//                        currentPath.points.last(),
//                        msg.point
//                    )
//                )
//            )
//        } else {
        paths[paths.count() - 1] =
            currentPath.copy(points = currentPath.points + point)
//        }
        return copy(paths = paths)
    }
}