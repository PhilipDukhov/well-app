package com.well.sharedMobile.puerh._topLevel

import com.well.modules.napier.Napier
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.State.*
import com.well.modules.utils.base.withEmptySet

typealias ReducerResult = Pair<State, Set<TopLevelFeature.Eff>>

private inline fun <reified R : ScreenState> Map<Tab, List<ScreenState>>.screenAndPositionOfTopOrNull(
    state: State,
): Pair<R, ScreenPosition>? {
    keys.sortedBy { tab ->
        if (state.selectedScreenPosition.tab == tab) {
            1
        } else {
            0
        }
    }.forEach { tab ->
        getValue(tab).withIndex().reversed().forEach {
            val (index, screen) = it
            if (screen is R) {
                return screen to ScreenPosition(tab, index)
            }
        }
    }
    return null
}

internal fun State.copyReplace(
    tab: Tab = selectedScreenPosition.tab,
    screen: ScreenState,
): State {
    val (tabs, screenPosition) = tabs.replace(tab, screen)
    return copy(
        tabs = tabs,
        selectedScreenPosition = screenPosition
    )
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

private fun Map<Tab, List<ScreenState>>.replace(
    tab: Tab,
    screen: ScreenState,
) = modify(tab) {
    it.dropLast(1) + screen
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

@Suppress("UNCHECKED_CAST")
internal inline fun <reified SS : ScreenState, M, S, E> State.reduceScreen(
    msg: M,
    reducer: (M, S) -> Pair<S, Set<E>>,
    effCreator: (E) -> TopLevelFeature.Eff,
): ReducerResult {
    val (screen, position) = tabs.screenAndPositionOfTopOrNull<SS>(this)
        ?: run {
            Napier.e("reduceScreen $msg | $this")
            return this.withEmptySet()
        }
    val (newScreenState, effs) = reducer(msg, screen.baseState as S)
    val newEffs = effs.mapTo(HashSet(), effCreator)
    return changeScreen<SS>(position) {
        baseCopy(newScreenState as Any) as SS
    } to newEffs
}