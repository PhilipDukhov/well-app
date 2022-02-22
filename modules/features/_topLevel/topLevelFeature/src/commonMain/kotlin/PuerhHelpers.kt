package com.well.modules.features.topLevel.topLevelFeature

import com.well.modules.features.calendar.calendarFeature.CalendarFeature
import com.well.modules.features.chatList.chatListFeature.ChatListFeature
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.more.moreFeature.MoreFeature
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.Eff
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State.ScreenPosition
import com.well.modules.features.topLevel.topLevelFeature.TopLevelFeature.State.Tab
import com.well.modules.models.User

internal typealias ReducerResult = Pair<State, Set<Eff>>

internal fun <ChildState> State.copyReplace(
    tab: Tab = selectedScreenPosition.tab,
    state: ChildState,
    createScreen: (ScreenPosition, ChildState) -> ScreenState,
): State {
    val (tabs, screenPosition) = tabs.replace(tab, state, createScreen)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun <ChildState> State.copyPush(
    tab: Tab = selectedScreenPosition.tab,
    state: ChildState,
    createScreen: (ScreenPosition, ChildState) -> ScreenState,
): State {
    val (tabs, screenPosition) = tabs.push(tab, state, createScreen)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun State.copyPopToRoot(
    tab: Tab = selectedScreenPosition.tab,
): State {
    val (tabs, screenPosition) = tabs.popToRoot(tab)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun State.copyPop(
    tab: Tab = selectedScreenPosition.tab,
    fallbackTab: Tab = Tab.Experts,
): State {
    if (selectedScreenPosition.index == 0 && tab.isTabBar()) {
        return this
    }
    val (tabs, screenPosition) = tabs.pop(tab, fallbackTab)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun State.copyHideTab(
    tab: Tab,
    fallbackTab: Tab = Tab.Experts,
): State {
    val (tabs, screenPosition) = tabs.modify(tab, fallbackTab) {
        listOf()
    }
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

private fun <ChildState> Map<Tab, List<ScreenState>>.push(
    tab: Tab,
    state: ChildState,
    createScreen: (ScreenPosition, ChildState) -> ScreenState,
) = modify(tab) {
    it + createScreen(ScreenPosition(tab = tab, index = it.indices.last + 1), state)
}

private fun Map<Tab, List<ScreenState>>.pop(
    tab: Tab,
    fallbackTab: Tab,
) = modify(tab, fallbackTab) {
    it.dropLast(1)
}

private fun Map<Tab, List<ScreenState>>.popToRoot(
    tab: Tab,
) = modify(tab) {
    listOf(it.first())
}

private fun <ChildState> Map<Tab, List<ScreenState>>.replace(
    tab: Tab,
    state: ChildState,
    createScreen: (ScreenPosition, ChildState) -> ScreenState,
) = modify(tab) {
    it.dropLast(1) + createScreen(ScreenPosition(tab = tab, index = it.indices.last), state)
}

private fun Map<Tab, List<ScreenState>>.modify(
    tab: Tab,
    fallbackTab: Tab? = null,
    block: (List<ScreenState>) -> List<ScreenState>,
) = toMutableMap()
    .let { mutableTabs ->
        val newTabList = block(mutableTabs[tab] ?: listOf())
        val resultTab: Tab
        if (newTabList.isEmpty()) {
            mutableTabs.remove(tab)
            resultTab = fallbackTab!!
        } else {
            mutableTabs[tab] = newTabList
            resultTab = tab
        }
        mutableTabs.toMap() to ScreenPosition(
            resultTab,
            mutableTabs.getValue(resultTab).lastIndex
        )
    }

internal fun <T : ScreenState> Map<Tab, List<ScreenState>>.copy(
    screenPosition: ScreenPosition,
    block: T.() -> T,
): Map<Tab, List<ScreenState>> = toMutableMap().also { mutableTabs ->
    mutableTabs[screenPosition.tab] = mutableTabs
        .getValue(screenPosition.tab)
        .toMutableList()
        .also {
            @Suppress("UNCHECKED_CAST")
            it[screenPosition.index] = block(it[screenPosition.index] as T)
        }
}

internal fun State.reduceScreenChanged() = this to setOf(
//    Eff.TopScreenDisappeared(oldState.topScreen, oldState.selectedScreenPosition),
    Eff.TopScreenAppeared(topScreen, selectedScreenPosition),
)

internal fun <Eff, FE : FeatureEff> Set<Eff>.mapTo(
    featureEff: (Eff, ScreenPosition) -> FE,
    position: ScreenPosition,
): Set<FE> = mapTo(HashSet()) {
    featureEff(it, position)
}

internal fun <State, SS : ScreenState> createTab(
    tab: Tab,
    state: State,
    createScreen: (ScreenPosition, State) -> SS,
) =
    tab to listOf(
        createScreen(ScreenPosition(tab, index = 0), state)
    )

internal fun loggedInTabs(uid: User.Id) = mapOf(
    createTab(
        Tab.MyProfile,
        state = MyProfileFeature.initialState(
            isCurrent = true,
            uid = uid
        ),
        createScreen = ScreenState::MyProfile,
    ),
    createTab(
        tab = Tab.Experts,
        state = ExpertsFeature.initialState(),
        createScreen = ScreenState::Experts,
    ),
    createTab(
        tab = Tab.ChatList,
        state = ChatListFeature.State(),
        createScreen = ScreenState::ChatList,
    ),
    createTab(
        tab = Tab.Calendar,
        state = CalendarFeature.State(),
        createScreen = ScreenState::Calendar,
    ),
    createTab(
        tab = Tab.More,
        state = MoreFeature.State(),
        createScreen = ScreenState::More,
    ),
)