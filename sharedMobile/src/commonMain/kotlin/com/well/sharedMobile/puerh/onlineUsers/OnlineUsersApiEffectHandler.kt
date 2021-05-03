package com.well.sharedMobile.puerh.onlineUsers

import com.well.modules.atomic.asCloseable
import com.well.sharedMobile.networking.NetworkManager
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature.Eff
import com.well.sharedMobile.puerh.onlineUsers.OnlineUsersFeature.Msg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OnlineUsersApiEffectHandler(
    private val networkManager: NetworkManager,
    override val coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    init {
        networkManager.apply {
            listOf(
                coroutineScope.launch {
                    state.collect {
                        listener?.invoke(Msg.OnConnectionStatusChange(it))
                    }
                },
                coroutineScope.launch {
                    currentUser.collect {
                        listener?.invoke(Msg.OnCurrentUserUpdated(it))
                    }
                },
            ).forEach {
                addCloseableChild(it.asCloseable())
            }
        }
    }

    override fun handleEffect(eff: Eff) {
        addCloseableChild(
            coroutineScope.launch {
                when (eff) {
                    is Eff.UpdateList -> {
                        listener?.invoke(
                            Msg.OnUsersUpdated(
                                networkManager.filteredUsersList(eff.filter)
                            )
                        )
                    }
                    is Eff.SetUserFavorite -> {
                        networkManager.setFavorite(eff.setter)
                    }
                    else -> Unit
                }
            }.asCloseable()
        )
    }
}