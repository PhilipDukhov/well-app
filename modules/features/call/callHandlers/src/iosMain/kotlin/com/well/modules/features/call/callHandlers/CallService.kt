package com.well.modules.features.call.callHandlers

import com.well.modules.atomic.AtomicRef
import com.well.modules.atomic.Closeable
import com.well.modules.atomic.asCloseable
import com.well.modules.atomic.freeze
import com.well.modules.features.call.callFeature.CallEndedReason
import com.well.modules.models.CallInfo
import com.well.modules.models.Notification
import com.well.modules.models.NotificationToken
import com.well.modules.utils.kotlinUtils.toJsonElement
import com.well.modules.utils.viewUtils.getCodeNameString
import com.well.modules.utils.viewUtils.resumeWithOptionalError
import com.well.modules.utils.viewUtils.toHexEncodedString
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import platform.CallKit.CXAction
import platform.CallKit.CXAnswerCallAction
import platform.CallKit.CXCallController
import platform.CallKit.CXCallEndedReason
import platform.CallKit.CXCallEndedReasonFailed
import platform.CallKit.CXCallEndedReasonRemoteEnded
import platform.CallKit.CXCallEndedReasonUnanswered
import platform.CallKit.CXCallUpdate
import platform.CallKit.CXEndCallAction
import platform.CallKit.CXErrorCodeIncomingCallErrorCallUUIDAlreadyExists
import platform.CallKit.CXErrorCodeIncomingCallErrorFilteredByBlockList
import platform.CallKit.CXErrorCodeIncomingCallErrorFilteredByDoNotDisturb
import platform.CallKit.CXErrorCodeIncomingCallErrorUnentitled
import platform.CallKit.CXErrorCodeIncomingCallErrorUnknown
import platform.CallKit.CXErrorDomainIncomingCall
import platform.CallKit.CXHandle
import platform.CallKit.CXHandleTypeGeneric
import platform.CallKit.CXProvider
import platform.CallKit.CXProviderConfiguration
import platform.CallKit.CXProviderDelegateProtocol
import platform.CallKit.CXSetMutedCallAction
import platform.CallKit.CXStartCallAction
import platform.CallKit.CXTransaction
import platform.Foundation.NSBundle
import platform.Foundation.NSError
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

actual fun createCallService(
    services: CallService.Services,
    parentCoroutineScope: CoroutineScope,
): CallService = object : CallService() {
    private val coroutineScope: CoroutineScope
    private var ongoingCallId by AtomicRef<NSUUID?>()

    init {
        val job = Job()
        coroutineScope = parentCoroutineScope + job
        addCloseableChild(job.asCloseable())
    }

    override suspend fun reportNewIncomingCall(callInfo: CallInfo) {
        runCatching {
            suspendCancellableCoroutine<Unit> { continuation ->
                reportNewIncomingCall(callInfo = callInfo, completion = continuation::resumeWithOptionalError.freeze())
            }
        }
    }

    override fun callStartedConnecting() {
        val callId = ongoingCallId ?: return
        callProvider.reportOutgoingCallWithUUID(callId, startedConnectingAtDate = null)
    }

    override fun endCall(reason: CallEndedReason) {
        val callId = ongoingCallId ?: return
        val cxReason = reason.toCXCallEndedReason()
        if (cxReason != null) {
            callProvider.reportCallWithUUID(callId, endedAtDate = null, reason = cxReason)
        } else {
            coroutineScope.launch {
                val endCallAction = CXEndCallAction(callUUID = callId)
                requestTransaction(endCallAction)
            }
        }
    }

    override fun reportNewOutgoingCall(callInfo: CallInfo) {
        coroutineScope.launch {
            val callId = callInfo.id.uuid.nativeUuid
            ongoingCallId = callId

            val handle = CXHandle(CXHandleTypeGeneric, value = callInfo.user.fullName)
            val startCallAction = CXStartCallAction(callId, handle)
            startCallAction.contactIdentifier = callInfo.user.fullName
            startCallAction.video = callInfo.hasVideo

            requestTransaction(startCallAction)

            val update = CXCallUpdate()
            update.remoteHandle = handle
            update.localizedCallerName = callInfo.user.fullName

            callProvider.reportCallWithUUID(callId, updated = update)
        }
    }

    override fun reportOutgoingCallConnected() {
        val callId = ongoingCallId ?: return
        callProvider.reportOutgoingCallWithUUID(callId, connectedAtDate = null)
    }

    override fun updateCallHasVideo(hasVideo: Boolean) {
        val callId = ongoingCallId ?: return
        val callUpdate = CXCallUpdate()
        callUpdate.hasVideo = hasVideo
        callProvider.reportCallWithUUID(callId, updated = callUpdate)
    }

    private fun reportNewIncomingCall(callInfo: CallInfo, completion: (NSError?) -> Unit) {
        val callUpdate = CXCallUpdate()
        callUpdate.remoteHandle = CXHandle(CXHandleTypeGeneric, value = callInfo.user.fullName)
        callUpdate.hasVideo = callInfo.hasVideo
        callUpdate.supportsHolding = false
        completion.freeze()
        val callId = callInfo.id.uuid.nativeUuid
        callProvider.reportNewIncomingCallWithUUID(
            UUID = callId,
            update = callUpdate,
            completion = { error: NSError? ->
                completion(error)
                if (error != null) {
                    val readableError = error.toCXErrorDomainIncomingCallString()
                    Napier.e("reportNewIncomingCall failed: ${readableError ?: error}")
                } else {
                    ongoingCallId = callId
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
            services.updateToken(createApnsToken(didUpdatePushCredentials.token.toHexEncodedString()))
        }

        override fun pushRegistry(registry: PKPushRegistry, didInvalidatePushTokenForType: PKPushType) {
            services.updateToken(createApnsToken(null))
        }

        private fun createApnsToken(token: String?) =
            NotificationToken.ApnsVoip(voipToken = token, bundleId = NSBundle.mainBundle.bundleIdentifier!!)

        override fun pushRegistry(
            registry: PKPushRegistry,
            didReceiveIncomingPushWithPayload: PKPushPayload,
            forType: PKPushType,
            withCompletionHandler: () -> Unit,
        ) {
            val payloadJson = didReceiveIncomingPushWithPayload
                .dictionaryPayload[Notification.payloadDataKey]
                .toJsonElement()

            when (val notification = Json.decodeFromJsonElement<Notification.Voip>(payloadJson)) {
                is Notification.Voip.IncomingCall -> {
                    if (ongoingCallId == null) {
                        reportNewIncomingCall(
                            callInfo = notification,
                            completion = { error ->
                                withCompletionHandler()
                                if (error == null) {
                                    services.reportNewCall(notification.webSocketMsg)
                                }
                            },
                        )
                    } else {
                        Napier.i("Ignoring incoming call notification $notification: ongoing callId: $ongoingCallId ")
                    }
                }
                is Notification.Voip.CanceledCall -> {
                    if (ongoingCallId == notification.callId.uuid.nativeUuid) {
                        callProvider.invalidate()
                        services.reportCallCancelled(notification)
                    } else {
                        Napier.i("Ignoring cancel call notification $notification")
                    }
                }
            }
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
        addCloseableChild(
            object : Closeable {
                override fun close() {
                    voipRegistry.delegate = null
                    callProvider.setDelegate(null, queue = null)
                    callProvider.invalidate()
                    voipRegistry.desiredPushTypes = emptySet<String>()
                }
            }
        )
    }

    private fun CallEndedReason.toCXCallEndedReason(): CXCallEndedReason? =
        when (this) {
            CallEndedReason.Failed -> CXCallEndedReasonFailed
            CallEndedReason.RemoteEnded -> CXCallEndedReasonRemoteEnded
            CallEndedReason.Unanswered -> CXCallEndedReasonUnanswered
            CallEndedReason.Finished -> null
        }

    private fun NSError.toCXErrorDomainIncomingCallString(): String? =
        getCodeNameString(
            domain = CXErrorDomainIncomingCall,
            codes = listOf(
                ::CXErrorCodeIncomingCallErrorCallUUIDAlreadyExists,
                ::CXErrorCodeIncomingCallErrorFilteredByBlockList,
                ::CXErrorCodeIncomingCallErrorFilteredByDoNotDisturb,
                ::CXErrorCodeIncomingCallErrorUnentitled,
                ::CXErrorCodeIncomingCallErrorUnknown,
            )
        )
}