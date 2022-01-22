package com.well.modules.features.calendar.calendarHandlers

import com.well.modules.features.calendar.calendarFeature.CalendarFeature.Eff
import com.well.modules.features.calendar.calendarFeature.CalendarFeature.Msg
import com.well.modules.models.Meeting
import com.well.modules.models.MeetingViewModel
import com.well.modules.models.User
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.print
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class CalendarEffHandler(
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    data class Services(
        val currentUserId: User.Id,
        val meetingsFlow: Flow<List<Meeting>>,
        val getUsersByIdsFlow: (Set<User.Id>) -> Flow<List<User>>,
        val openUserProfile: (User.Id) -> Unit,
        val startCall: (User.Id) -> Unit,
        val updateMeetingState: (Meeting.Id, Meeting.State) -> Unit,
    )

    private val meetingsFlow = services
        .meetingsFlow
        .print { "meetingsFlow $it" }
        .flatMapLatest { meetings ->
            services
                .getUsersByIdsFlow(
                    meetings
                        .map { it.otherUid(services.currentUserId) }
                        .toSet()
                )
                .print { "getUsersByIdsFlow $it" }
                .map { users ->
                    val groupedUsers = users
                        .groupBy(User::id)
                        .mapValues { it.value.first() }
                    meetings.mapNotNull { meeting ->
                        val otherUserId = meeting.otherUid(services.currentUserId)
                        val otherUser = groupedUsers[otherUserId] ?: return@mapNotNull null
                        MeetingViewModel(
                            id = meeting.id,
                            state = meeting.state,
                            isExpert = meeting.expertUid == services.currentUserId,
                            startInstant = meeting.startInstant,
                            durationMinutes = meeting.durationMinutes,
                            otherUser = otherUser,
                        )
                    }
                        .filter {
                            !it.isExpert || it.state != Meeting.State.Rejected
                        }
                }
        }

    init {
        meetingsFlow
            .map(Msg::UpdateMeetings)
            .print { "final meetingsFlow $it" }
            .collectIn(effHandlerScope, ::listener)
    }

    override suspend fun processEffect(eff: Eff) {
        when (eff) {
            is Eff.OpenUserProfile -> {
                services.openUserProfile(eff.uid)
            }
            is Eff.StartCall -> {
                services.startCall(eff.uid)
            }
            is Eff.UpdateMeetingState -> {
                services.updateMeetingState(eff.meetingId, eff.state)
            }
        }
    }
}