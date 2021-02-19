package com.well.sharedMobile.puerh._topLevel

import com.well.utils.platform.Platform
import com.well.utils.platform.isDebug
import com.well.utils.userReadableDescription

sealed class Alert(
    val title: String = "",
    val description: String = "",
    val positiveAction: Action? = null,
    val negativeAction: Action? = null,
) {
    object CameraOrMicDenied : Alert(
        title = Strings.callDenied,
        positiveAction = Action.Ok,
        negativeAction = Action.Settings
    )

    data class Throwable(val throwable: kotlin.Throwable) :
        Alert(
            title = throwable.userReadableDescription()
                ?: (if (Platform.isDebug) "${throwable::class} $throwable" else Strings.somethingWentWrong),
            description = if (Platform.isDebug) "${throwable::class} $throwable" else "",
            positiveAction = Action.Ok
        )

    sealed class Action(val title: String) {
        object Ok : Action(Strings.ok)
        object Settings : Action(Strings.settings)
    }
}