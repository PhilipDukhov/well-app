package com.well.modules.features.calendar.calendarHandlers

import com.well.modules.features.calendar.calendarFeature.CalendarFeature.Eff
import com.well.modules.features.calendar.calendarFeature.CalendarFeature.Msg
import com.well.modules.models.Meeting
import com.well.modules.models.MeetingViewModel
import com.well.modules.models.User
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.mapIterable
import com.well.modules.utils.viewUtils.SuspendAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class CalendarEffHandler(
    private val services: Services,
    parentCoroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(parentCoroutineScope) {
    class Services(
        val currentUserId: User.Id,
        val meetingsFlow: Flow<List<Meeting>>,
        val getUsersByIdsFlow: (Set<User.Id>) -> Flow<List<User>>,
        val openUserProfile: (User.Id) -> Unit,
        val startCall: (User.Id) -> Unit,
        val updateMeetingState: (Meeting.Id, Meeting.State) -> Unit,
        val showSheet: (actions: Array<SuspendAction>, title: String) -> Unit,
    )

    private val meetingsFlow = services
        .meetingsFlow
        .flatMapLatest { meetings ->
            services
                .getUsersByIdsFlow(
                    meetings
                        .map { it.otherUid(services.currentUserId) }
                        .toSet()
                )
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
                            it.state !is Meeting.State.Canceled
                                    && (!it.isExpert || it.state !is Meeting.State.Rejected)
                        }
                }
        }

    init {
        meetingsFlow
            .map(Msg::UpdateMeetings)
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