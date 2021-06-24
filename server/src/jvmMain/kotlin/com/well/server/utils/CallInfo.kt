package com.well.server.utils

import com.well.modules.models.UserId

data class CallInfo(val uids: List<UserId>) {
    constructor(vararg uids: UserId) : this(uids.toList())
}