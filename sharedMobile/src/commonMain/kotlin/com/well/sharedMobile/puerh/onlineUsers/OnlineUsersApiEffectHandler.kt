package com.well.sharedMobile.puerh.onlineUsers

import com.well.atomic.asCloseable
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature.Msg.*
import com.well.utils.puerh.EffectHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OnlineUsersApiEffectHandler(
    networkManager: NetworkManager,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<OnlineUsersFeature.Eff, OnlineUsersFeature.Msg>(coroutineScope) {
    init {
        networkManager.apply {
            listOf(
                coroutineScope.launch {
                    state.collect {
                        listener?.invoke(OnConnectionStatusChange(it))
                    }
                },
                coroutineScope.launch {
                    onlineUsers.collect {
                        listener?.invoke(OnUsersUpdated(it))
                    }
                },
                coroutineScope.launch {
                    currentUser.collect {
                        listener?.invoke(OnCurrentUserUpdated(it))
                    }
                },
            ).forEach {
                addCloseableChild(it.asCloseable())
            }
        }
    }

    override fun handleEffect(eff: OnlineUsersFeature.Eff) {
    }
}