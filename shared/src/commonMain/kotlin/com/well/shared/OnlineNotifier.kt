package com.well.shared

import com.github.aakira.napier.Napier
import com.well.serverModels.User
import com.well.shared.networking.PlatformSocket
import com.well.shared.networking.PlatformSocketListener
import io.ktor.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@KtorExperimentalAPI
class OnlineNotifier(token: String) {
    private val ws = PlatformSocket(
        "Bearer $token",
        "ws://dukhovwellserver.com:8090/onlineUsers",
    )

    fun subscribeOnOnlineUsers(block: (List<User>) -> Unit) {
        ws.openSocket(object: PlatformSocketListener {
            override fun onOpen() {
                Napier.e("onOpen")
            }

            override fun onFailure(t: Throwable) {
                Napier.e("onFailure $t")
            }

            override fun onMessage(msg: String) {
                Napier.e("onMessage $msg")
                block(Json.decodeFromString(msg))
            }

            override fun onClosing(code: Int, reason: String) {
                Napier.e("onClosing $code $reason")
            }

            override fun onClosed(code: Int, reason: String) {
                Napier.e("onClosed $code $reason")
            }
        })
    }
}