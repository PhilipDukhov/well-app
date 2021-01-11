package com.well.sharedMobile.networking

import com.well.sharedMobile.puerh.toNSData
import com.well.utils.resumeWithException
import com.well.utils.toThrowable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.native.concurrent.freeze

actual class WebSocketSession(
    private val webSocket: NSURLSessionWebSocketTask,
) {
    private val channel = Channel<String>(8)
    actual val incoming: ReceiveChannel<String> = channel

    actual suspend fun send(text: String) =
        send(NSURLSessionWebSocketMessage(text))

    actual suspend fun send(data: ByteArray) =
        send(NSURLSessionWebSocketMessage(data.toNSData()))

    private suspend fun send(message: NSURLSessionWebSocketMessage) =
        suspendCancellableCoroutine<Unit> { continuation ->
            webSocket.sendMessage(message, { error: NSError? ->
                if (error != null) {
                    continuation.resumeWithException(error)
                } else {
                    continuation.resume(Unit)
                }
            }.freeze())
        }

    private fun handler(
        message: NSURLSessionWebSocketMessage?,
        nsError: NSError?
    ) {
        when {
            nsError != null -> {
                channel.close(nsError.toThrowable())
            }
            message != null -> {
                message.string?.let {
                    GlobalScope.launch {
                        channel.send(it)
                    }
                }
                listenMessages()
            }
        }
    }

    fun listenMessages() =
        webSocket.receiveMessageWithCompletionHandler(::handler.freeze())

    fun close(t: Throwable?) =
        channel.close(t)
}
