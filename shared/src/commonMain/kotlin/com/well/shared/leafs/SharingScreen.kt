package com.well.shared.leafs

import com.well.serverModels.Color
import com.well.serverModels.Path
import com.well.serverModels.Point
import com.well.serverModels.Screen
import com.well.shared.leafs.SharingScreen.Model.Guest
import com.well.shared.leafs.SharingScreen.Model.Host
import com.well.shared.leafs.SharingScreen.Msg.*
import oolong.Effect
import oolong.effect.none

object SharingScreen {
    enum class State {
        Idle,
        Host,
        Guest,
    }

    sealed class Model(open val userId: String) {
        data class Host(
            override val userId: String,
            val screen: Screen,
//            val screensHistory: ArrayDeque<Screen> = ArrayDeque(listOf(screen)),
        ) : Model(userId)

        data class Guest(
            override val userId: String,
            val screen: Screen? = null,
        ) : Model(userId)
    }

    sealed class Msg {
        object BecomeHost : Msg()
        data class ServerScreenUpdated(val screen: Screen?) : Msg()
        class UploadImageData(val data: ByteArray, val fileExtension: String) : Msg()
        data class UpdateImagePath(val path: String) : Msg()

        data class StartPath(val color: Color) : Msg()
        data class AddPoint(val point: Point) : Msg()
//        data class FinishPath(val point: Point) : Msg()
    }

    class Props(
        val screen: Screen? = null,
        currentUserId: String,
    ) {
        val state = when {
            screen == null ->
                State.Idle
            screen.hostId == currentUserId -> State.Host
            else -> State.Guest
        }
    }

    val init = { userId: String ->
        {
            Guest(userId) to none<Msg>()
        }
    }

    val update = update@{ msg: Msg, model: Model ->
        when (model) {
            is Host -> model.copy(screen = model.screen.run screen@{
                when (msg) {
                    is BecomeHost -> throw IllegalStateException()

                    is StartPath -> return@screen copy(paths = paths + Path(listOf(), msg.color))
                    is AddPoint -> return@screen addPoint(msg)
                    is ServerScreenUpdated -> {
                        if (msg.screen?.hostId == model.screen.hostId) {
                            return@update model to none<Msg>()
                        }
                        return@update Guest(model.userId, msg.screen) to none<Msg>()
                    }
                    is UploadImageData -> {
                        return@update model to uploadImageData(msg)
                    }
                    is UpdateImagePath -> {
                        return@screen copy(imageURL = msg.path)
                    }
                }
            })
            is Guest -> when (msg) {
                is StartPath,
                is AddPoint,
                is UploadImageData,
                is UpdateImagePath ->
                    throw IllegalStateException()
                is ServerScreenUpdated -> {
                    if (msg.screen?.hostId == model.userId) {
                        return@update Host(model.userId, msg.screen) to none<Msg>()
                    }
                    model.copy(screen = msg.screen)
                }
                is BecomeHost -> Host(model.userId, Screen(model.userId))
            }
        } to none<Msg>()
    }

    val view = { model: Model ->
        model.run {
            Props(
                screen = when (this) {
                    is Host -> screen
                    is Guest -> screen
                }?.run {
                    copy(paths = paths.filter { it.points.isNotEmpty() })
                },
                currentUserId = userId
            )
        }
    }

    private fun Screen.addPoint(msg: AddPoint): Screen {
        val currentPath = paths.last()
        val paths = paths.toMutableList()
        if (currentPath.points.count() > 100) {
            paths.add(
                currentPath.copy(
                    points = listOf(
                        currentPath.points.last(),
                        msg.point
                    )
                )
            )
        } else {
            paths[paths.count() - 1] =
                currentPath.copy(points = currentPath.points + msg.point)
        }
        return copy(paths = paths)
    }

    private fun uploadImageData(msg: UploadImageData): Effect<Msg> = {
        try {
            TODO()
//            val path = FirebaseManager.manager.upload(
//                msg.data,
//                "mountains.${msg.fileExtension}"
//            )
//            println(path)
//            UpdateImagePath(path)
        } catch (e: Throwable) {
            throw e
        }
    }
}