package com.well.shared.puerh.onlineUsers

import com.well.shared.puerh.WebSocketManager
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature.Eff.ConnectToServer
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature.Msg.OnConnectionStatusChange
import com.well.shared.puerh.onlineUsers.OnlineUsersFeature.Msg.OnUsersUpdated
import com.well.utils.EffectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class OnlineUsersApiEffectHandler(
    private val webSocketManager: WebSocketManager,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<OnlineUsersFeature.Eff, OnlineUsersFeature.Msg>(coroutineScope) {
    init {
        webSocketManager.apply {
            coroutineScope.launch {
                state.collect {
                    listener?.invoke(OnConnectionStatusChange(it))
                }
            }
            coroutineScope.launch {
                onlineUsers.collect {
                    println("tesststst2 $it")
                    listener?.invoke(OnUsersUpdated(it))
                }
            }
        }
    }

    override fun handleEffect(eff: OnlineUsersFeature.Eff) {
        when (eff) {
            ConnectToServer -> {
                webSocketManager.connect()
            }
            else -> Unit
        }
    }
}