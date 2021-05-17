package com.well.sharedMobile.puerh.experts

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.asCloseable
import com.well.sharedMobile.networking.NetworkManager
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Eff
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Msg
import com.well.sharedMobile.puerh.experts.filter.FilterFeature
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class ExpertsApiEffectHandler(
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

    private var filteredUsersListJob = AtomicRef<Job>()
    override fun handleEffect(eff: Eff) {
        addCloseableChild(
            coroutineScope.launch {
                when (eff) {
                    is Eff.UpdateList -> {
                        filteredUsersListJob.value?.cancel()
                        filteredUsersListJob.value = launch {
                            try {
                                listener?.invoke(
                                    Msg.OnUsersUpdated(
                                        networkManager.filteredUsersList(eff.filter)
                                    )
                                )
                            } catch (cancel: CancellationException) {
                            }
                        }
                    }
                    is Eff.SetUserFavorite -> {
                        networkManager.setFavorite(eff.setter)
                    }
//                    is Eff.FilterEff -> {
//                        when (eff.eff) {
//                            is FilterFeature.Eff.Show -> {
//                                listener?.invoke(
//                                    Msg.FilterMsg()
//                                )
//                            }
//                        }
//                    }
                    else -> Unit
                }
            }.asCloseable()
        )
    }
}