package com.well.sharedMobile.networking.webSocketManager

import com.well.sharedMobile.networking.RedirectResponseException
import com.well.sharedMobile.networking.getThrowable
import com.well.utils.freeze
import com.well.utils.resumeWithException
import io.ktor.client.features.ClientRequestException
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
                println("didCompleteWithError $throwable $didCompleteWithError")
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
        delegate.webSocketSession = WebSocketSession(webSocket).freeze()
        delegate.freeze()
        webSocket.resume()
    })