package com.well.sharedMobile.puerh.topLevel

private const val callDenied =
    "Well needs access to your microphone and camera so that you can make video calls"

sealed class Alert(
    val description: String,
    val positiveAction: Action,
    val negativeAction: Action,
) {
    object CameraOrMicDenied : Alert(callDenied, Action.Ok, Action.Settings)

    sealed class Action(val title: String) {
        object Ok : Action("OK")
        object Settings : Action("Settings")
    }
}