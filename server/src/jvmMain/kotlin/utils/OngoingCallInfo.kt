package com.well.server.utils

import com.well.modules.models.User

data class OngoingCallInfo(
    val activeClients: List<ClientKey>,
    val pendingUsers: List<User.Id>,
) {
    val uids = activeClients.map { it.uid } + pendingUsers
}