package com.well.sharedMobile.puerh.experts

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.asCloseable
import com.well.modules.db.users.Database
import com.well.modules.db.users.toUser
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.models.UsersFilter
import com.well.modules.models.WebSocketMsg
import com.well.modules.utils.puerh.EffectHandler
import com.well.sharedMobile.networking.NetworkManager
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Eff
import com.well.sharedMobile.puerh.experts.ExpertsFeature.Msg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ExpertsApiEffectHandler(
    private val networkManager: NetworkManager,
    private val database: Database,
    override val coroutineScope: CoroutineScope,
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
                .map { list ->
                    list.map { it.toUser() }
                }
        }
    init {
        networkManager.apply {
            listOf(
                coroutineScope.launch {
                    state.collect {
                        listener?.invoke(Msg.OnConnectionStatusChange(it))
                    }
                }.asCloseable(),
                addListener { webSocketMsg ->
                    if (webSocketMsg is WebSocketMsg.Back.ListFilteredExperts) {
                        coroutineScope.launch {
                            filteredExpertsIdsFlow.emit(webSocketMsg.userIds)
                        }
                    }
                },
                coroutineScope.launch {
                    filteredExpertsUsersFlow.collect {
                        listener?.invoke(Msg.OnUsersUpdated(it))
                    }
                }.asCloseable(),
                coroutineScope.launch {
                    filterFlow
                        .collect {
                            networkManager.send(WebSocketMsg.Front.SetExpertsFilter(it))
                        }
                }.asCloseable(),
            ).forEach(::addCloseableChild)
        }
    }

    override fun handleEffect(eff: Eff) {
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