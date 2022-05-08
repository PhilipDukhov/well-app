package com.well.modules.features.notifications

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.CloseableContainerImpl
import com.well.modules.atomic.asCloseable
import com.well.modules.atomic.freeze
import com.well.modules.models.CallInfo
import com.well.modules.models.Notification
import com.well.modules.utils.kotlinUtils.toJsonElement
import com.well.modules.utils.viewUtils.resumeWithOptionalError
import com.well.modules.utils.viewUtils.toHexEncodedString
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import platform.CallKit.CXAction
import platform.CallKit.CXAnswerCallAction
import platform.CallKit.CXCallController
import platform.CallKit.CXCallUpdate
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXHandle
import platform.CallKit.CXHandleTypeGeneric
import platform.CallKit.CXProvider
import platform.CallKit.CXProviderConfiguration
import platform.CallKit.CXProviderDelegateProtocol
import platform.CallKit.CXSetMutedCallAction
import platform.CallKit.CXStartCallAction
import platform.CallKit.CXTransaction
import platform.Foundation.NSError
import platform.Foundation.NSLog
import platform.Foundation.NSUUID
import platform.PushKit.PKPushCredentials
import platform.PushKit.PKPushPayload
import platform.PushKit.PKPushRegistry
import platform.PushKit.PKPushRegistryDelegateProtocol
import platform.PushKit.PKPushType
import platform.PushKit.PKPushTypeVoIP
import platform.darwin.NSObject
import platform.darwin.dispatch_queue_attr_make_with_qos_class
import platform.darwin.dispatch_queue_create
import platform.posix.QOS_CLASS_USER_INITIATED

internal actual class CallService actual constructor(
    private val services: CallServiceServices,
    parentCoroutineScope: CoroutineScope,
) : CloseableContainerImpl() {
    private val coroutineScope: CoroutineScope
    private var ongoingCallId by AtomicRef<NSUUID?>()

    init {
        val job = Job()
        coroutineScope = parentCoroutineScope + job
        addCloseableChild(job.asCloseable())
    }

    actual suspend fun reportNewIncomingCall(callInfo: CallInfo) {
        suspendCancellableCoroutine<Unit> { continuation ->
            reportNewIncomingCall(callInfo = callInfo, completion = continuation::resumeWithOptionalError.freeze())
        }
    }

    actual fun updateCallHasVideo(hasVideo: Boolean) {
        val callId = ongoingCallId ?: return
        val callUpdate = CXCallUpdate()
        callUpdate.hasVideo = hasVideo
        callProvider.reportCallWithUUID(callId, updated = callUpdate)
    }

    actual suspend fun reportNewOutgoingCall(callInfo: CallInfo) {
        val callId = callInfo.id.nativeUuid
        ongoingCallId = callId

        val handle = CXHandle(CXHandleTypeGeneric, value = callInfo.senderName)
        val startCallAction = CXStartCallAction(callId, handle)
        startCallAction.contactIdentifier = callInfo.senderName
        startCallAction.video = callInfo.hasVideo

        requestTransaction(startCallAction)

        val update = CXCallUpdate()
        update.remoteHandle = handle
        update.localizedCallerName = callInfo.senderName

        callProvider.reportCallWithUUID(callId, updated = update)
    }

    actual fun reportOutgoingCallConnected() {
        val callId = ongoingCallId ?: return
        callProvider.reportOutgoingCallWithUUID(callId, startedConnectingAtDate = null)
    }

    private fun reportNewIncomingCall(callInfo: CallInfo, completion: (NSError?) -> Unit) {
        val callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = CXHandle(CXHandleTypeGeneric, value = callInfo.senderName)
        callUpdate.hasVideo = callInfo.hasVideo
        callUpdate.supportsHolding = false
        completion.freeze()
        callProvider.reportNewIncomingCallWithUUID(
            UUID = callInfo.id.nativeUuid,
            update = callUpdate,
            completion = { error: NSError? ->
                completion(error)
                if (error != null) {
                    Napier.e("reportNewIncomingCall failed: $error")
                } else {
                    ongoingCallId = callInfo.id.nativeUuid
                }
            }.freeze()
        )
    }

    private suspend fun requestTransaction(action: CXAction) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val transaction = CXTransaction(action)
            callController.requestTransaction(transaction, completion = continuation::resumeWithOptionalError.freeze())
        }
    }

    private val pushRegistryDelegate = object : NSObject(), PKPushRegistryDelegateProtocol {
        override fun pushRegistry(
            registry: PKPushRegistry,
            didUpdatePushCredentials: PKPushCredentials,
            forType: PKPushType,
        ) {
            coroutineScope.launch {
                services.updateToken(didUpdatePushCredentials.token.toHexEncodedString())
            }
        }

        override fun pushRegistry(registry: PKPushRegistry, didInvalidatePushTokenForType: PKPushType) {
            services.updateToken(null)
        }

        override fun pushRegistry(
            registry: PKPushRegistry,
            didReceiveIncomingPushWithPayload: PKPushPayload,
            forType: PKPushType,
            withCompletionHandler: () -> Unit,
        ) {
            val payloadJson = didReceiveIncomingPushWithPayload
                .dictionaryPayload[Notification.payloadDataKey]
                .toJsonElement()
            val notification = Json.run {
                decodeFromJsonElement<Notification.IncomingCall>(serializersModule.serializer(), payloadJson)
            }
            reportNewIncomingCall(
                callInfo = notification,
                completion = { error ->
                    withCompletionHandler()
                    if (error != null) {
                        Napier.e("reportNewIncomingCall failed: $error")
                    } else {
                        services.reportNewCall(notification)
                    }
                },
            )
        }
    }

    private val providerDelegate = object : NSObject(), CXProviderDelegateProtocol {
        override fun providerDidReset(provider: CXProvider) {
            ongoingCallId = null
        }

        override fun provider(provider: CXProvider, executeTransaction: CXTransaction): Boolean {
            executeTransaction.actions
                .filterIsInstance<CXAction>()
                .forEach { action ->
                    when (action) {
                        is CXStartCallAction -> {
                            Napier.d("Should we handle CXStartCallAction?")
                        }
                        is CXAnswerCallAction -> {
                            services.answer()
                        }
                        is CXEndCallAction -> {
                            services.decline()
                        }
                        is CXSetMutedCallAction -> {
                            services.setMuted(action.muted)
                        }
                        else -> return@forEach
                    }
                    action.fulfill()
                }
            return true
        }
    }

    private val callProvider: CXProvider
    private val voipRegistry: PKPushRegistry
    private val callController: CXCallController

    init {
        val callKitProviderConfig = CXProviderConfiguration()
        callKitProviderConfig.supportsVideo = true
        callKitProviderConfig.supportedHandleTypes = setOf(CXHandleTypeGeneric)
        callKitProviderConfig.maximumCallsPerCallGroup = 1U
        callKitProviderConfig.maximumCallGroups = 1U
        callProvider = CXProvider(configuration = callKitProviderConfig)

        val queue = dispatch_queue_create(
            "CallServiceQueue",
            dispatch_queue_attr_make_with_qos_class(null, QOS_CLASS_USER_INITIATED, 0)
        )
        voipRegistry = PKPushRegistry(queue = queue)
        voipRegistry.delegate = pushRegistryDelegate

        callController = CXCallController(queue)

        callProvider.setDelegate(providerDelegate, queue = queue)
        coroutineScope.launch {
            // this can call delegate method immediately,
            // which will cause an exception as object initialization is not yet finished
            voipRegistry.desiredPushTypes = setOf(PKPushTypeVoIP)
        }
    }
}