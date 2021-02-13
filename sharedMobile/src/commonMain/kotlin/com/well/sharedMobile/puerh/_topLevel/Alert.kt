package com.well.sharedMobile.puerh._topLevel

import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug

sealed class Alert(
    val title: String,
    val description: String,
    val positiveAction: Action? = null,
    val negativeAction: Action? = null,
) {
    object CameraOrMicDenied : Alert(
        title = Strings.callDenied,
        description = "",
        positiveAction = Action.Ok,
        negativeAction = Action.Settings
    )

    data class Throwable(val throwable: kotlin.Throwable) :
        Alert(
            title = Strings.somethingWentWrong,
            description = if (Platform.isDebug) throwable.toString() else "",
            positiveAction = Action.Ok
        )

    sealed class Action(val title: String) {
        object Ok : Action(Strings.ok)
        object Settings : Action(Strings.settings)
    }
}