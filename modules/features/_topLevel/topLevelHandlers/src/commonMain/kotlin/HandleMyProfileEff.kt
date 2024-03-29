package com.well.modules.features.topLevel.topLevelHandlers

import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.myProfile.myProfileHandlers.MyProfileEffHandler
import com.well.modules.features.topLevel.topLevelFeature.FeatureEff
import com.well.modules.features.topLevel.topLevelFeature.FeatureMsg
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Eff
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Msg
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State
import com.well.modules.models.User
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.puerhBase.Listener
import com.well.modules.puerhBase.adapt
import com.well.modules.utils.kotlinUtils.launchedIn
import com.well.modules.utils.kotlinUtils.map
import com.well.modules.utils.viewUtils.Alert
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

internal fun TopLevelFeatureProviderImpl.createProfileEffHandler(
    uid: User.Id,
    user: User?,
    isCurrent: Boolean = false,
    position: State.ScreenPosition,
    listener: Listener<Msg>,
): EffectHandler<Eff, Msg> = MyProfileEffHandler(
    services = MyProfileEffHandler.Services(
        userFlow = if (user?.initialized == false) flowOf(user) else getFullUserByIdFlow(uid),
        putUser = networkManager::putUser,
        uploadProfilePicture = networkManager::uploadProfilePicture,
        showExceptionAlert = {
            listener(
                Msg.ShowAlert(
                    Alert.Error.exceptionAlert(it)
                )
            )
        },
        onInitializationFinished = {
            loggedIn(nonInitializedAuthInfo.value, listener = listener)
        },
        onPop = { listener(Msg.Pop) },
        setFavorite = ::updateUserFavorite.launchedIn(coroutineScope),
        onStartCall = { _user, hasVideo ->
            listener(Msg.StartCall(_user, hasVideo))
        },
        onOpenUserChat = listener.map(Msg::OpenUserChat),
        onLogout = {
            coroutineScope.launch {
                logOut()
                listener.invoke(Msg.OpenLoginScreen)
            }
        },
        onDeleteProfile = {
            MainScope().launch {
                systemHelper?.showAlert(
                    Alert.Custom(
                        title = "Confirmation",
                        description = "Are you sure you want to delete your account? This operation cannot be undone",
                        positiveAction = Alert.Action.Cancel,
                        negativeAction = Alert.Action.Custom("Delete") {
                            coroutineScope.launch {
                                logOut()
                                networkManager.deleteProfile()
                                listener.invoke(Msg.OpenLoginScreen)
                            }
                        }
                    )
                )
            }
        },
        openTechSupport = {
            TODO()
        },
        requestBecomeExpert = networkManager::requestBecomeExpert
            .launchedIn(coroutineScope),
        onRatingRequest = {
            coroutineScope.launch {
                networkManager.rate(it)
                listener(FeatureMsg.Experts(ExpertsFeature.Msg.Reload, position))
            }
        },
        getCurrentUserAvailabilities = networkManager::listCurrentUserAvailabilities,
        addAvailability = networkManager::putAvailability,
        removeAvailability = networkManager::removeAvailability,
        updateAvailability = networkManager::putAvailability,
        book = networkManager::book,
        getUserAvailabilitiesToBook = { networkManager.getAvailabilities(uid) },
        hasAvailableAvailabilities = {
            if (isCurrent) {
                false
            } else {
                networkManager.userHasAvailableAvailabilities(uid)
            }
        },
    ),
    systemHelper = systemHelper!!,
    parentCoroutineScope = coroutineScope,
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