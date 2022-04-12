package com.well.modules.utils.viewUtils

import com.well.modules.atomic.Closeable
import com.well.modules.utils.viewUtils.sharedImage.LocalImage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

expect class SystemHelper(systemContext: SystemContext) : WebAuthenticator {
    val systemContext: SystemContext
    fun showAlert(alert: Alert)
    fun showSheet(actions: List<Action>, title: String?): Closeable
    fun openUrl(url: String)
    override suspend fun webAuthenticate(url: String, requestCode: Int): String
    suspend fun pickSystemImage(): LocalImage
}

fun SystemHelper.showSheetThreadSafe(
    vararg actions: SuspendAction,
    title: String = "",
) {
    MainScope().launch {
        showSheet(
            title = title,
            actions = actions.map {
                Action(it.title) {
                    CoroutineScope(Dispatchers.Default).launch {
                        it.action()
                    }
                }
            },
        )
    }
}

suspend fun SystemHelper.pickSystemImageSafe(): LocalImage? = try {
    pickSystemImage()
} catch (t: Throwable) {
    Napier.e("pickSystemImageSafe failed", t)
    null
}