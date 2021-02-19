package com.well.androidApp.call

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.PhoneAccountHandle
import com.well.napier.Napier

class ConnectionService: android.telecom.ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Napier.d("onCreateIncomingConnection $request")
        return super.onCreateIncomingConnection(connectionManagerPhoneAccount, request)
    }
}