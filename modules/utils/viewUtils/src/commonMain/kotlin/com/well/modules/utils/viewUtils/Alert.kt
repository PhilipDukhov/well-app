package com.well.modules.utils.viewUtils

import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.isDebug

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

    data class Error(
        val throwable: Throwable,
        val errorDescription: String,
    ) : Alert(
        title = errorDescription,
        description = if (Platform.isDebug) "${errorDescription::class} $errorDescription" else "",
        positiveAction = Action.Ok
    ) {
        constructor(
            throwable: Throwable,
            descriptionBuilder: (Throwable) -> String?,
        ) : this(
            throwable = throwable,
            errorDescription = descriptionBuilder(throwable)
                ?: throwable.message
                ?: "${throwable::class} $throwable"
        )
        companion object
    }

    sealed class Action(val title: String) {
        object Ok : Action(Strings.ok)
        object Settings : Action(Strings.settings)
    }
}