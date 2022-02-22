package com.well.modules.features.notifications

import com.well.modules.models.User
import android.content.Intent
import io.github.aakira.napier.Napier

fun NotificationHandler.handleOnNewIntent(intent: Intent): Boolean {
    if (intent.action != SystemNotificationHelper.openChatAction) {
        return false
    }
    val userId = intent.getLongExtra(SystemNotificationHelper.userIdKey, Long.MIN_VALUE)
    if (userId != Long.MIN_VALUE) {
        services.openChat(User.Id(userId))
    } else {
        Napier.e("unexpected intent $intent")
    }
    return true
}