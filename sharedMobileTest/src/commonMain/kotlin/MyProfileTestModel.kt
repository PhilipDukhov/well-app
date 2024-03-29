package com.well.sharedMobileTest

import com.well.modules.atomic.AtomicRef
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Eff
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.State
import com.well.modules.features.myProfile.myProfileHandlers.MyProfileEffHandler
import com.well.modules.models.Availability
import com.well.modules.models.BookingAvailability
import com.well.modules.models.User
import com.well.modules.puerhBase.ReducerViewModel
import com.well.modules.utils.viewUtils.SystemHelper
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

class MyProfileTestModel(
    isCurrent: Boolean,
    systemHelper: SystemHelper,
) : ReducerViewModel<State, Msg, Eff>(
    MyProfileFeature.testState(isCurrent),
    MyProfileFeature::reducer,
) {
    private var setAvailabilitiesCounter by AtomicRef(0)
    private var bookingCounter by AtomicRef(0)
    private var updateAvailabilitiesCounter by AtomicRef(0)

    private val handler by lazy {
        MyProfileEffHandler(
            systemHelper = systemHelper,
            services = MyProfileEffHandler.Services(
                userFlow = flowOf(User.testUser),
                putUser = {},
                uploadProfilePicture = { error("unimplemented") },
                showExceptionAlert = { Napier.e(it.message ?: it.toString(), it) },
                onInitializationFinished = {},
                onPop = {},
                setFavorite = {},
                onStartCall = {},
                onOpenUserChat = {},
                onLogout = {},
                requestBecomeExpert = {},
                onRatingRequest = {},
                getCurrentUserAvailabilities = {
                    if (setAvailabilitiesCounter++ % 2 == 0)
                        Availability.testValues(20)
                    else
                        error("load availabilities error")
                },
                addAvailability = { it },
                updateAvailability = { it },
                removeAvailability = { },
                hasAvailableAvailabilities = { !state.value.isCurrent },
                book = {
                    delay(3000)
                    if (bookingCounter++ % 2 == 0) {
                        throw IllegalStateException("some error")
                    }
                },
                getUserAvailabilitiesToBook = {
                    delay(3000)
                    if (updateAvailabilitiesCounter++ % 2 == 0)
                        listOf()
                    else
                        BookingAvailability.createWithAvailabilities(
                            Availability.testValues(10),
                            days = 30,
                            minInterval = 1.hours,
                        )
                },
                onDeleteProfile = {},
                openTechSupport = {},
            ),
            parentCoroutineScope = coroutineScope
        )
    }

    override fun handleEffs(effs: Set<Eff>) {
        effs.forEach { eff ->
            handler.handleEffect(eff)
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        coroutineScope.launch {
            handler.setListener(::listener)
        }
    }
}