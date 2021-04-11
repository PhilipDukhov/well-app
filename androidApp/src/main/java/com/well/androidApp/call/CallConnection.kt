package com.well.androidApp.call

import android.os.Build
import android.telecom.CallAudioState
import android.telecom.Connection
import com.well.modules.napier.Napier

class CallConnection: Connection() {
    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectionProperties = PROPERTY_SELF_MANAGED
        }
        audioModeIsVoip = true
    }

    override fun onCallAudioStateChanged(state: CallAudioState?) {
        Napier.i("CallConnection onCallAudioStateChanged $state")
    }
}