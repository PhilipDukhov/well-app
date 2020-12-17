package com.well.shared.networking

import com.well.utils.resumeWithException
import com.well.utils.toNSData
import com.well.utils.toThrowable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import platform.Foundation.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
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
        suspendCoroutine<Unit> { continuation ->
            webSocket.sendMessage(message) {
                if (it != null) {
                    continuation.resumeWithException(it)
                } else {
                    continuation.resume(Unit)
                }
            }
        }

    private fun handler(message: NSURLSessionWebSocketMessage?, nsError: NSError?) {
        NSLog("res ${message?.string} $nsError")
        webSocket.sendPingWithPongReceiveHandler {

        }
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