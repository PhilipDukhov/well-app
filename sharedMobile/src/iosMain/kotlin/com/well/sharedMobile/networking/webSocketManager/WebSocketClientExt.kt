package com.well.sharedMobile.networking.webSocketManager

import com.well.modules.atomic.freeze
import com.well.modules.utils.toThrowable
import com.well.sharedMobile.networking.getThrowable
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSNumber
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSString
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSURLSessionWebSocketCloseCode
import platform.Foundation.NSURLSessionWebSocketDelegateProtocol
import platform.Foundation.NSURLSessionWebSocketTask
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.addValue
import platform.Foundation.create
import platform.darwin.NSObject
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    webSocket(path, block)
}

private suspend fun WebSocketClient.webSocket(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) = coroutineContext.let { coroutineContext ->
    // replace with suspendCoroutine when https://github.com/Kotlin/kotlinx.coroutines/issues/2363 fixed
    block(suspendCancellableCoroutine { continuation ->
        val components = NSURLComponents()
        components.scheme = config.webSocketProtocol.name
        components.host = config.host
        components.port = config.port?.let { NSNumber(it) }
        components.path = path
        val delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
            lateinit var webSocketSession: WebSocketSession

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didOpenWithProtocol: String?,
            ) {
                webSocketSession.listenMessages()
                continuation.resume(webSocketSession)
            }

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didCloseWithCode: NSURLSessionWebSocketCloseCode,
                reason: NSData?,
            ) {
                webSocketSession.close(
                    Throwable(
                        "NSURLSessionWebSocketTask closed code: $didCloseWithCode reason: ${
                            reason?.let {
                                NSString.create(
                                    it,
                                    NSUTF8StringEncoding
                                )
                            }
                        }"
                    )
                )
            }

            override fun URLSession(
                session: NSURLSession,
                task: NSURLSessionTask,
                didCompleteWithError: NSError?,
            ) {
                val throwable = (task.response as? NSHTTPURLResponse)
                    ?.let { getThrowable(it.statusCode.toInt()) }
                    ?: didCompleteWithError?.toThrowable()
                    ?: CancellationException("${task.response}")
                Napier.e("ios web socket didCompleteWithError $didCompleteWithError", throwable)
                if (continuation.isActive) {
                    continuation.resumeWithException(throwable)
                } else {
                    webSocketSession.close(throwable)
                }
            }
        }
        val urlSession = NSURLSession.sessionWithConfiguration(
            configuration = NSURLSessionConfiguration.defaultSessionConfiguration(),
            delegate = delegate,
            delegateQueue = NSOperationQueue.currentQueue(),
        )
        val webSocket = urlSession.webSocketTaskWithRequest(
            NSMutableURLRequest(components.URL!!).apply {
                config.header.let {
                    addValue(it.second, it.first)
                }
            }
        )
        delegate.webSocketSession =
            WebSocketSession(webSocket, CoroutineScope(coroutineContext)).freeze()
        delegate.freeze()
        webSocket.resume()
    })
}