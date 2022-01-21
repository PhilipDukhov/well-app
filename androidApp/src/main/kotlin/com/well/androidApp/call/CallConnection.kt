package com.well.androidApp.call

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.TelecomManager
import io.github.aakira.napier.Napier

class CallConnection : Connection() {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionProperties = PROPERTY_SELF_MANAGED
        }
        audioModeIsVoip = true
        setCallerDisplayName("setCallerDisplayName", TelecomManager.PRESENTATION_ALLOWED)
    }

    override fun onShowIncomingCallUi() {
        Napier.i("onShowIncomingCallUi")
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Napier.i("onCallAudioStateChanged $state")
    }
}