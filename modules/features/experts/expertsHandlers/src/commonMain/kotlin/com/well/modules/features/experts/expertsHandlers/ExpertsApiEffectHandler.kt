package com.well.modules.features.experts.expertsHandlers

import com.well.modules.atomic.asCloseable
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.Eff
import com.well.modules.features.experts.expertsFeature.ExpertsFeature.Msg
import com.well.modules.models.ConnectionStatus
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.User
import com.well.modules.models.UsersFilter
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.combineWithUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ExpertsApiEffectHandler(
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    data class Services(
        val connectionStatusFlow: Flow<ConnectionStatus>,
        val usersListFlow: Flow<List<User>>,
        val updateUsersFilter: suspend (UsersFilter?) -> Unit,
        val onConnectedFlow: Flow<Unit>,
        val setFavorite: suspend (FavoriteSetter) -> Unit,
    )

    private val inputFilterFlow = MutableStateFlow<UsersFilter?>(null)
    private val filterFlow = inputFilterFlow.debounce(300)

    init {
        services.connectionStatusFlow
            .map(Msg::OnConnectionStatusChange)
            .collectIn(coroutineScope, action = ::listener)
        services.usersListFlow
            .map(Msg::OnUsersUpdated)
            .collectIn(coroutineScope, action = ::listener)
        filterFlow
            .combineWithUnit(services.onConnectedFlow)
            .collectIn(coroutineScope) {
                services.updateUsersFilter(it)
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
                        services.setFavorite(eff.setter)
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