package com.well.shared.networking

import com.well.shared.OnlineNotifier
import com.well.utils.freeze
import com.well.utils.resumeWithException
import com.well.utils.toThrowable
import io.ktor.http.*
import kotlinx.coroutines.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.*

internal actual suspend fun WebSocketClient.ws(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    webSocket(URLProtocol.WS, path, block)
}

internal actual suspend fun WebSocketClient.wss(
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) {
    webSocket(URLProtocol.WSS, path, block)
}

private suspend fun WebSocketClient.webSocket(
    protocol: URLProtocol,
    path: String,
    block: suspend WebSocketSession.() -> Unit,
) =
    // replace with suspendCoroutine when https://github.com/Kotlin/kotlinx.coroutines/issues/2363 fixed
    block(suspendCancellableCoroutine { continuation ->
        val components = NSURLComponents()
        components.scheme = protocol.name
        components.host = config.host
        components.port = NSNumber(config.port)
        components.path = path
        val delegate = object : NSObject(), NSURLSessionWebSocketDelegateProtocol {
            lateinit var webSocketSession: WebSocketSession

            override fun URLSession(
                session: NSURLSession,
                webSocketTask: NSURLSessionWebSocketTask,
                didOpenWithProtocol: String?
            ) {
                webSocketSession.listenMessages()
                continuation.resume(webSocketSession)
            }

            override fun URLSession(
                session: NSURLSession,
                task: NSURLSessionTask,
                didCompleteWithError: NSError?,
            ) {
                if (continuation.isActive) {
                    didCompleteWithError
                        ?.let { continuation.resumeWithException(it) }
                        ?: continuation.cancel()
                } else {
                    webSocketSession.close(didCompleteWithError?.toThrowable())
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
        delegate.webSocketSession = WebSocketSession(webSocket).freeze()
        delegate.freeze()
        webSocket.resume()
    })