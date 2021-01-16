package com.well.sharedMobile.puerh.topLevel

import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State
import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.*

typealias ReducerResult = Pair<State, Set<TopLevelFeature.Eff>>

internal inline fun <reified R : ScreenState> Map<Tab, List<ScreenState>>.screenAndPositionOfFirstOrNull(
): Pair<R, ScreenPosition>? {
    for ((tab, screens) in this) {
        for ((index, screen) in screens.withIndex()) {
            if (screen is R) {
                return screen to ScreenPosition(tab, index)
            }
        }
    }
    return null
}

internal fun State.copyPush(
    tab: Tab,
    screen: ScreenState,
): State {
    val (tabs, screenPosition) = tabs.push(tab, screen)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun State.copyPop(
    tab: Tab = selectedScreenPosition.tab,
    fallbackTab: Tab = Tab.Main,
): State {
    val (tabs, screenPosition) = tabs.pop(tab, fallbackTab)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

internal fun State.copyHideTab(
    tab: Tab,
    fallbackTab: Tab = Tab.Main,
): State {
    val (tabs, screenPosition) = tabs.modify(tab, fallbackTab) {
        listOf()
    }
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
}

private fun Map<Tab, List<ScreenState>>.push(
    tab: Tab,
    screen: ScreenState,
) = modify(tab) {
    it + screen
}

private fun Map<Tab, List<ScreenState>>.pop(
    tab: Tab,
    fallbackTab: Tab,
) = modify(tab, fallbackTab) {
    it.dropLast(1)
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
): Map<Tab, List<ScreenState>> =
    toMutableMap().also { mutableTabs ->
        mutableTabs[screenPosition.tab] = mutableTabs
            .getValue(screenPosition.tab)
            .toMutableList()
            .also {
                @Suppress("UNCHECKED_CAST")
                it[screenPosition.index] = block(it[screenPosition.index] as T)
            }
    }