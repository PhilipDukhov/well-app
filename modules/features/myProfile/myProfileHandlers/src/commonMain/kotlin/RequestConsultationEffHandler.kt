package com.well.modules.features.myProfile.myProfileHandlers

import com.well.modules.models.BookingAvailabilitiesListByDay
import com.well.modules.models.BookingAvailability
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature as Feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

internal class RequestConsultationEffHandler(
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Feature.Eff, Feature.Msg>(parentCoroutineScope) {
    data class Services(
        val closeConsultationRequest: () -> Unit,
        val book: suspend (BookingAvailability) -> Unit,
        val getAvailabilitiesByDay: suspend () -> BookingAvailabilitiesListByDay,
        val gotEmptyAvailabilities: () -> Unit,
    )

    override suspend fun processEffect(eff: Feature.Eff) {
        when (eff) {
            is Feature.Eff.Close -> {
                delay(eff.timeoutMillis)
                services.closeConsultationRequest()
            }
            is Feature.Eff.Book -> {
                try {
                    services.book(eff.availability)
                    listener(Feature.Msg.Booked)
                } catch (e: Exception) {
                    Feature.Msg.BookingFailed(
                        reason = e.toString(),
                        newAvailabilities = null
                    )
                }
            }
            Feature.Eff.Update -> {
                val availabilities = services.getAvailabilitiesByDay()
                listener(Feature.Msg.UpdateAvailabilitiesByDay(availabilities))
            }
            is Feature.Eff.ClearFailedState -> {
                delay(eff.timeoutMillis)
                listener(Feature.Msg.ClearFailedState)
            }
        }
    }

}