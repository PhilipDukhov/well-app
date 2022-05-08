package com.well.androidApp.call

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import io.github.aakira.napier.Napier

class CallConnectionService: ConnectionService() {
    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ): Connection {
        Napier.i("onCreateOutgoingConnection $request")
        return com.well.modules.features.notifications.CallConnection()
    }

    override fun onCreateIncomingConferenceFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ) {
        Napier.i("onCreateIncomingConferenceFailed $request")
    }

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ): Connection {
        Napier.i("onCreateIncomingConnection $request")
        return com.well.modules.features.notifications.CallConnection()
    }

    override fun onCreateIncomingConnectionFailed(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?,
    ) {
        Napier.i("onCreateIncomingConnectionFailed $request")
    }
}