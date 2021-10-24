@file:Suppress("UNCHECKED_CAST")

package com.well.androidAppTest

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable

@Composable
internal fun ColumnScope.MyProfileTest() {
//    val viewModel = viewModel<TestViewModel>(factory = object : ViewModelProvider.Factory {
//        override fun <T : ViewModel?> create(modelClass: Class<T>): T =
//            TestViewModel() as T
//    })
//    MyProfileScreen(
//        state = viewModel.state,
//        listener = viewModel::listener
//    )
}

//private class TestViewModel : ViewModel() {
//    var state by mutableStateOf(Feature.testState(false))
//        private set
//
//    fun listener(msg: Msg) {
//        val (newState, effs) = Feature.reducer(msg, state)
//        state = newState
//        println("$msg ${newState.requestConsultationState}")
//        effHandler(effs)
//    }
//
//    private fun effHandler(effs: Set<Eff>) {
//        effs.forEach { eff ->
//            when (eff) {
//                is Eff.RequestConsultationEff -> {
//                    consultationJobs += viewModelScope.launch {
//                        requestConsultationEffHandler(eff.eff)
//                    }
//                }
//                is Eff.CloseConsultationRequest -> consultationJobs.forEach { it.cancel() }
//                else -> Unit
//            }
//        }
//    }
//
//    private val consultationJobs = mutableListOf<Job>()
//
//    private var bookingCounter = 0
//
//    private suspend fun requestConsultationEffHandler(eff: RequestConsultationFeature.Eff) {
//        when (eff) {
//            is RequestConsultationFeature.Eff.Close -> {
//                delay(eff.timeoutMillis)
//                listener(Msg.CloseConsultationRequest)
//            }
//            is RequestConsultationFeature.Eff.Book -> {
//                delay(3000)
//                if (bookingCounter % 2 == 0) {
//                    listener(
//                        Msg.RequestConsultationMsg(
//                            RequestConsultationFeature.Msg.BookingFailed(
//                                reason = "some error",
//                                newAvailabilities = null
//                            )
//                        )
//                    )
//                } else {
//                    listener(Msg.RequestConsultationMsg(RequestConsultationFeature.Msg.Booked))
//                }
//                bookingCounter += 1
//            }
//            RequestConsultationFeature.Eff.Update -> {
//                delay(500)
//                listener(
//                    Msg.RequestConsultationMsg(
//                        RequestConsultationFeature.Msg.UpdateAvailabilities(
//                            Availability.testValues
//                        )
//                    )
//                )
//            }
//            is RequestConsultationFeature.Eff.ClearFailedState -> {
//                delay(eff.timeoutMillis)
//                listener(Msg.RequestConsultationMsg(RequestConsultationFeature.Msg.ClearFailedState))
//            }
//        }
//    }
//}