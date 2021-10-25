package com.well.sharedMobileTest

import com.well.modules.models.Availability
import com.well.modules.puerhBase.ReducerViewModel
import com.well.modules.features.myProfile.MyProfileFeature
import com.well.modules.features.myProfile.MyProfileFeature.Eff
import com.well.modules.features.myProfile.MyProfileFeature.Msg
import com.well.modules.features.myProfile.MyProfileFeature.State
import com.well.modules.features.myProfile.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import com.well.modules.features.myProfile.currentUserAvailability.RequestConsultationFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyProfileTestModel(isCurrent: Boolean) : ReducerViewModel<State, Msg, Eff>(
    MyProfileFeature.testState(isCurrent),
    MyProfileFeature::reducer,
) {
    init {
        if (state.value.isCurrent) {
            listener(
                Msg.AvailabilityMsg(
                    CurrentUserAvailabilitiesListFeature.Msg.SetAvailabilities(
                        Availability.testValues
                    )
                )
            )
        }
    }

    override fun handleEffs(effs: Set<Eff>) {
        effs.forEach { eff ->
            when (eff) {
                is Eff.RequestConsultationEff -> {
                    val job = Job()
                    CoroutineScope(Dispatchers.Main + job).launch {
                        requestConsultationEffHandler(eff.eff)
                    }
                    consultationJobs += job
                }
                is Eff.CloseConsultationRequest -> consultationJobs.forEach { it.cancel() }
                else -> Unit
            }
        }
    }

    private val consultationJobs = mutableListOf<Job>()

    private var bookingCounter = 0

    private suspend fun requestConsultationEffHandler(eff: RequestConsultationFeature.Eff) {
        when (eff) {
            is RequestConsultationFeature.Eff.Close -> {
                delay(eff.timeoutMillis)
                listener(Msg.CloseConsultationRequest)
            }
            is RequestConsultationFeature.Eff.Book -> {
                delay(3000)
                if (bookingCounter % 2 == 0) {
                    listener(
                        Msg.RequestConsultationMsg(
                            RequestConsultationFeature.Msg.BookingFailed(
                                reason = "some error",
                                newAvailabilities = null
                            )
                        )
                    )
                } else {
                    listener(Msg.RequestConsultationMsg(RequestConsultationFeature.Msg.Booked))
                }
                bookingCounter += 1
            }
            RequestConsultationFeature.Eff.Update -> {
                delay(500)
                listener(
                    Msg.RequestConsultationMsg(
                        RequestConsultationFeature.Msg.UpdateAvailabilities(
                            Availability.testValues
                        )
                    )
                )
            }
            is RequestConsultationFeature.Eff.ClearFailedState -> {
                delay(eff.timeoutMillis)
                listener(Msg.RequestConsultationMsg(RequestConsultationFeature.Msg.ClearFailedState))
            }
        }
    }
}