package com.well.modules.features.experts.expertsHandlers

import com.well.modules.atomic.asCloseable
import com.well.modules.db.users.Users
import com.well.modules.db.users.UsersDatabase
import com.well.modules.db.users.toUser
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.Eff
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.Msg
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.State
import com.well.modules.models.UserId
import com.well.modules.models.UsersFilter
import com.well.modules.models.WebSocketMsg
import com.well.modules.networking.NetworkManager
import com.well.modules.networking.combineToNetworkConnectedState
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.mapIterable
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ExpertsApiEffectHandler(
    private val networkManager: NetworkManager,
    private val database: UsersDatabase,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    private val filteredExpertsIdsFlow = MutableSharedFlow<List<UserId>>()
    private val inputFilterFlow = MutableStateFlow<UsersFilter?>(null)
    private val filterFlow = inputFilterFlow.debounce(300)
    private val filteredExpertsUsersFlow = filteredExpertsIdsFlow
        .flatMapLatest { filteredExperts ->
            database.usersQueries
                .getByIds(filteredExperts)
                .asFlow()
                .mapToList()
                .mapIterable(Users::toUser)
        }

    init {
        networkManager.apply {
            listOf(
                coroutineScope.launch {
                    stateFlow.collect {
                        listener(Msg.OnConnectionStatusChange(
                            when (it) {
                                NetworkManager.Status.Disconnected -> State.ConnectionStatus.Disconnected
                                NetworkManager.Status.Connecting -> State.ConnectionStatus.Connecting
                                NetworkManager.Status.Connected -> State.ConnectionStatus.Connected
                            }
                        ))
                    }
                }.asCloseable(),
                coroutineScope.launch {
                    webSocketMsgSharedFlow
                        .filterIsInstance<WebSocketMsg.Back.ListFilteredExperts>()
                        .collect {
                            filteredExpertsIdsFlow.emit(it.userIds)
                        }
                }.asCloseable(),
                coroutineScope.launch {
                    filteredExpertsUsersFlow
                        .combineToNetworkConnectedState(networkManager)
                        .collect {
                            listener(Msg.OnUsersUpdated(it))
                        }
                }.asCloseable(),
                coroutineScope.launch {
                    filterFlow
                        .combineToNetworkConnectedState(networkManager)
                        .collect {
                            networkManager.sendFront(WebSocketMsg.Front.SetExpertsFilter(it))
                        }
                }.asCloseable(),
            ).forEach(::addCloseableChild)
        }
    }

    override suspend fun processEffect(eff: Eff) {
        addCloseableChild(
            coroutineScope.launch {
                when (eff) {
                    is Eff.UpdateList -> {
                        inputFilterFlow.emit(eff.filter)
                    }
                    is Eff.SetUserFavorite -> {
                        networkManager.setFavorite(eff.setter)
                    }
//                    is Eff.FilterEff -> {
//                        when (eff.eff) {
//                            is FilterFeature.Eff.Show -> {
//                                listener(
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