package com.well.sharedMobile.puerh._topLevel

import com.well.atomic.Closeable
import com.well.atomic.CloseableContainer
import com.well.atomic.asCloseable
import com.well.napier.Napier
import com.well.sharedMobile.utils.ImageContainer
import com.well.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

internal expect class ContextHelper(context: Context) {
    val context: Context
    fun showAlert(alert: Alert)
    fun showSheet(actions: List<Action>): Closeable
    fun openUrl(url: String)
    suspend fun pickSystemImage(): ImageContainer
}

internal suspend fun ContextHelper.showSheetThreadSafe(
    coroutineScope: CoroutineScope,
    vararg actions: SuspendAction,
): Closeable {
    val closeableContainer = CloseableContainer()
    closeableContainer.addCloseableChild(
        MainScope().launch {
            closeableContainer.addCloseableChild(
                showSheet(actions.map {
                    Action(it.title) {
                        coroutineScope.launch {
                            it.block()
                        }
                    }
                })
            )
        }.asCloseable()
    )
    return closeableContainer
}

internal suspend fun ContextHelper.pickSystemImageSafe(): ImageContainer? = try {
    pickSystemImage()
} catch (t: Throwable) {
    Napier.e("pickSystemImageSafe failed", t)
    null
}
