package com.well.modules.features.myProfile.myProfileHandlers

import com.well.modules.models.Availability
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature as Feature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay

class RequestConsultationEffHandler(
    private val services: Services,
    coroutineScope: CoroutineScope,
) : EffectHandler<Feature.Eff, Feature.Msg>(coroutineScope) {
    data class Services(
        val closeConsultationRequest: () -> Unit,
        val book: suspend (Availability) -> Unit,
        val getAvailabilities: suspend () -> List<Availability>,
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
                } catch (t: Throwable) {
                    Feature.Msg.BookingFailed(
                        reason = t.toString(),
                        newAvailabilities = null
                    )
                }
            }
            Feature.Eff.Update -> {
                val availabilities = services.getAvailabilities()
                listener(Feature.Msg.UpdateAvailabilities(availabilities))
                if (availabilities.isEmpty()) {
                    services.gotEmptyAvailabilities()
                }
            }
            is Feature.Eff.ClearFailedState -> {
                delay(eff.timeoutMillis)
                listener(Feature.Msg.ClearFailedState)
            }
        }
    }

}