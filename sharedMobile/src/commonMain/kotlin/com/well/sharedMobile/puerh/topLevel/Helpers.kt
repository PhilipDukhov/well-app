package com.well.sharedMobile.puerh.topLevel

import com.well.sharedMobile.puerh.topLevel.TopLevelFeature.State.*

typealias ReducerResult = Pair<TopLevelFeature.State, Set<TopLevelFeature.Eff>>

inline fun <reified R : ScreenState> Map<Tab, List<ScreenState>>.screenAndPositionOfFirstOrNull(
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

fun Map<Tab, List<ScreenState>>.push(
    tab: Tab,
    screen: ScreenState
): Map<Tab, List<ScreenState>> =
    toMutableMap()
        .also { mutableTabs ->
            mutableTabs[tab] = (mutableTabs[tab] ?: listOf()) + screen
        }

fun Map<Tab, List<ScreenState>>.pop(
    tab: Tab,
): Map<Tab, List<ScreenState>> =
    toMutableMap()
        .also { mutableTabs ->
            mutableTabs[tab]?.also {
                if (it.count() > 1) {
                    mutableTabs[tab] = it.dropLast(1)
                } else {
                    mutableTabs.remove(tab)
                }
            }
        }

fun Map<Tab, List<ScreenState>>.remove(
    tab: Tab,
): Map<Tab, List<ScreenState>> =
    toMutableMap()
        .also { mutableTabs ->
            mutableTabs.remove(tab)
        }

fun <T : ScreenState> Map<Tab, List<ScreenState>>.copy(
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