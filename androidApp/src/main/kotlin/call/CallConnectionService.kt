package com.well.androidApp.call

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.PhoneAccountHandle
import io.github.aakira.napier.Napier

class CallConnectionService: android.telecom.ConnectionService() {
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ): Connection {
        Napier.d("onCreateIncomingConnection $request")
        return CallConnection()
    }
}