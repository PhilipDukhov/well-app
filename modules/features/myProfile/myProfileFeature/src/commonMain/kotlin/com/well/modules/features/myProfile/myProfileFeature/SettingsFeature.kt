package com.well.modules.features.myProfile.myProfileFeature

import com.well.modules.puerhBase.toSetOf
import com.well.modules.utils.viewUtils.GlobalStringsBase

object SettingsFeature {
    object Strings: GlobalStringsBase() {
        const val logout = "Logout"
        const val deleteProfile = "Delete profile"
        const val technicalSupport = "Technical support"
    }

    data class State(
        val pushNotificationsEnabled: Boolean = true,
    )

    sealed class Msg {
        object DeleteProfile : Msg()
        object Logout : Msg()
        object OpenTechnicalSupport : Msg()
    }

    sealed interface Eff {
        object DeleteProfile : Eff
        object Logout : Eff
        object OpenTechnicalSupport : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
    ): Pair<State, Set<Eff>> = state toSetOf when (msg) {
        Msg.DeleteProfile -> {
            Eff.DeleteProfile
        }
        Msg.Logout -> {
            Eff.Logout
        }
        Msg.OpenTechnicalSupport -> {
            Eff.OpenTechnicalSupport
        }
    }
}
