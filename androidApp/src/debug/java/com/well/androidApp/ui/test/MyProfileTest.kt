@file:Suppress("UNCHECKED_CAST")

package com.well.androidApp.ui.test

import com.well.androidApp.ui.composableScreens.myProfile.MyProfileScreen
import com.well.modules.models.Availability
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Eff
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.myProfile.currentUserAvailability.RequestConsultationFeature
import com.well.sharedMobile.testData.testState
import com.well.sharedMobile.testData.testValues
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature as Feature
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun ColumnScope.MyProfileTest() {
    val viewModel = viewModel<TestViewModel>(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
            TestViewModel() as T
    })
    MyProfileScreen(
        state = viewModel.state,
        listener = viewModel::listener
    )
}

private class TestViewModel : ViewModel() {
    var state by mutableStateOf(Feature.testState(false))
        private set

    fun listener(msg: Msg) {
        val (newState, effs) = Feature.reducer(msg, state)
        state = newState
        println("$msg ${newState.requestConsultationState}")
        effHandler(effs)
    }

    private fun effHandler(effs: Set<Eff>) {
        effs.forEach { eff ->
            when (eff) {
                is Eff.RequestConsultationEff -> {
                    consultationJobs += viewModelScope.launch {
                        requestConsultationEffHandler(eff.eff)
                    }
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