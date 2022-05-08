package com.well.modules.features.notifications

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection
import android.telecom.TelecomManager
import android.telecom.VideoProfile
import io.github.aakira.napier.Napier

class CallConnection : Connection() {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionProperties = PROPERTY_SELF_MANAGED
        }
        audioModeIsVoip = true
        setCallerDisplayName("setCallerDisplayName", TelecomManager.PRESENTATION_ALLOWED)
        videoState = VideoProfile.STATE_BIDIRECTIONAL
    }

    override fun onShowIncomingCallUi() {
        Napier.i("onShowIncomingCallUi")
        setRinging()
        super.onShowIncomingCallUi()
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Napier.i("onCallAudioStateChanged $state")
        super.onCallAudioStateChanged(state)
    }

    override fun onAnswer(videoState: Int) {
        Napier.i("onAnswer $videoState")
        super.onAnswer(videoState)
    }

    override fun onReject() {
        Napier.i("onReject")
        super.onReject()
    }

    override fun onDisconnect() {
        Napier.i("onDisconnect")
        super.onDisconnect()
    }
}