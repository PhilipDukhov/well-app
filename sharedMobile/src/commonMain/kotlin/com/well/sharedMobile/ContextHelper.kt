package com.well.sharedMobile

import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import com.well.modules.utils.AppContext
import com.well.modules.utils.sharedImage.LocalImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal expect class ContextHelper(appContext: AppContext): WebAuthenticator {
    val appContext: AppContext
    fun showAlert(alert: Alert)
    fun showSheet(actions: List<Action>): Closeable
    fun openUrl(url: String)
    override suspend fun webAuthenticate(url: String, requestCode: Int): String
    suspend fun pickSystemImage(): LocalImage
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

internal suspend fun ContextHelper.pickSystemImageSafe(): LocalImage? = try {
    pickSystemImage()
} catch (t: Throwable) {
    Napier.e("pickSystemImageSafe failed", t)
    null
}