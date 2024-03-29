package com.well.modules.networking.webSocketManager

import com.well.modules.utils.viewUtils.resumeWithOptionalError
import com.well.modules.utils.viewUtils.toException
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.Foundation.NSURLSessionWebSocketMessage
import platform.Foundation.NSURLSessionWebSocketTask
import kotlin.native.concurrent.freeze

actual class WebSocketSession(
    private val webSocket: NSURLSessionWebSocketTask,
    private val scope: CoroutineScope,
) {
    private val channel = Channel<String>(8)
    actual val incoming: ReceiveChannel<String> = channel

    actual suspend fun send(text: String) =
        send(NSURLSessionWebSocketMessage(text))

    private suspend fun send(message: NSURLSessionWebSocketMessage) =
        suspendCancellableCoroutine<Unit> { continuation ->
            webSocket.sendMessage(message, continuation::resumeWithOptionalError.freeze())
        }

    private fun handler(
        message: NSURLSessionWebSocketMessage?,
        nsError: NSError?,
    ) {
        when {
            nsError != null -> {
                Napier.e("channel.close(nsError.toException()) $nsError ${nsError.localizedDescription}")
                channel.close(nsError.toException())
            }
            message != null -> {
                message.string?.let {
                    scope.launch {
                        channel.send(it)
                    }
                }
                listenMessages()
            }
        }
    }

    fun listenMessages() =
        webSocket.receiveMessageWithCompletionHandler(::handler.freeze())

    fun close(e: Exception?) =
        channel.close(e)
}