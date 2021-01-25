package com.well.androidApp.call

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection

class CallConnection: Connection() {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionProperties = PROPERTY_SELF_MANAGED
        }
        audioModeIsVoip = true
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        println("CallConnection onCallAudioStateChanged $state")
    }
}