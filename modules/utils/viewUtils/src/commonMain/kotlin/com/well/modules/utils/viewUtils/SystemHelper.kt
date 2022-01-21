package com.well.modules.utils.viewUtils

import com.well.modules.atomic.Closeable
import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

expect class SystemHelper(systemContext: SystemContext): WebAuthenticator {
    val systemContext: SystemContext
    fun showAlert(alert: Alert)
    fun showSheet(actions: List<Action>): Closeable
    fun openUrl(url: String)
    override suspend fun webAuthenticate(url: String, requestCode: Int): String
    suspend fun pickSystemImage(): LocalImage
}

suspend fun SystemHelper.showSheetThreadSafe(
    vararg actions: SuspendAction,
): Closeable {
    val context = coroutineContext
    val closeableContainer = CloseableContainer()
    closeableContainer.addCloseableChild(
        MainScope().launch {
            closeableContainer.addCloseableChild(
                showSheet(actions.map {
                    Action(it.title) {
                        CoroutineScope(context).launch {
                            it.block()
                        }
                    }
                })
            )
        }.asCloseable()
    )
    return closeableContainer
}

suspend fun SystemHelper.pickSystemImageSafe(): LocalImage? = try {
    pickSystemImage()
} catch (t: Throwable) {
    Napier.e("pickSystemImageSafe failed", t)
    null
}