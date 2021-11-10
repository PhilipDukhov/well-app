package com.well.sharedMobile.featureProvider

import com.well.modules.db.users.getByIdFlow
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.myProfile.myProfileHandlers.MyProfileEffHandler
import com.well.modules.models.UserId
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.puerhBase.Listener
import com.well.modules.puerhBase.adapt
import com.well.modules.utils.kotlinUtils.launchedIn
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.Alert
import com.well.sharedMobile.FeatureEff
import com.well.sharedMobile.FeatureMsg
import com.well.sharedMobile.TopLevelFeature.Eff
import com.well.sharedMobile.TopLevelFeature.Msg
import com.well.sharedMobile.TopLevelFeature.State
import kotlinx.coroutines.launch

internal fun FeatureProviderImpl.createProfileEffHandler(
    uid: UserId,
    position: State.ScreenPosition,
    listener: Listener<Msg>,
): EffectHandler<Eff, Msg> = MyProfileEffHandler(
    services = MyProfileEffHandler.Services(
        userFlow = usersDatabase.usersQueries.getByIdFlow(uid),
        putUser = networkManager::putUser,
        uploadProfilePicture = networkManager::uploadProfilePicture,
        showThrowableAlert = {
            listener(
                Msg.ShowAlert(
                    Alert.Error.fixDescription(it)
                )
            )
        },
        onInitializationFinished = {
            loggedIn(nonInitializedAuthInfo.value, listener = listener)
        },
        onPop = { listener(Msg.Pop) },
        setFavorite = networkManager::setFavorite.launchedIn(coroutineScope),
        onStartCall = listener.map(Msg::StartCall),
        onOpenUserChat = listener.map(Msg::OpenUserChat),
        onLogout = {
            logOut(listener)
            listener.invoke(Msg.OpenLoginScreen)
        },
        requestBecomeExpert = networkManager::requestBecomeExpert
            .launchedIn(coroutineScope),
        onRatingRequest = {
            coroutineScope.launch {
                networkManager.rate(it)
                listener(FeatureMsg.Experts(ExpertsFeature.Msg.Reload, position))
            }
        },
        addAvailability = networkManager::addAvailability,
        removeAvailability = networkManager::removeAvailability,
        updateAvailability = networkManager::updateAvailability,
        book = networkManager::book,
        getAvailabilities = { networkManager.getAvailabilities(uid) },
    ),
    contextHelper = contextHelper,
    coroutineScope = coroutineScope,
).adapt(
    effAdapter = { eff ->
        if (eff is FeatureEff.MyProfile && eff.position == position) {
            eff.eff
        } else {
            null
        }
    },
    msgAdapter = { FeatureMsg.MyProfile(msg = it, position = position) }
)