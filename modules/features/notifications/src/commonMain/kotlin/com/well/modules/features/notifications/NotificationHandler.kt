package com.well.modules.features.notifications

import com.well.modules.atomic.CloseableContainer
import com.well.modules.atomic.asCloseable
import com.well.modules.models.Notification
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageContainer
import com.well.modules.models.chat.ChatMessageViewModel
import com.well.modules.utils.flowUtils.MutableSetStateFlow
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.flowUtils.filterIterable
import com.well.modules.utils.flowUtils.filterNotEmpty
import com.well.modules.utils.flowUtils.flattenFlow
import com.well.modules.utils.flowUtils.print
import com.well.modules.utils.viewUtils.ApplicationContext
import com.well.modules.utils.viewUtils.RawNotification
import com.well.modules.utils.viewUtils.platform.Platform
import com.well.modules.utils.viewUtils.platform.current
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.plus
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class NotificationHandler(
    applicationContext: ApplicationContext,
    private val currentUid: User.Id,
    internal val services: Services,
    parentCoroutineScope: CoroutineScope,
) : CloseableContainer() {
    data class Services(
        val lastListViewModelFlow: Flow<List<ChatMessageContainer>>,
        val unreadCountsFlow: (List<ChatMessage>) -> Flow<Map<ChatMessage.Id, Long>>,
        val getUsersByIdsFlow: (List<User.Id>) -> Flow<List<User>>,
        val getMessageByIdFlow: (ChatMessage.Id) -> Flow<ChatMessageContainer>,
        val openChat: (User.Id) -> Unit,
    )

    private val notificationHelper = SystemNotificationHelper(applicationContext)
    private val coroutineScope: CoroutineScope
    private val currentNotificationsStateFlow = MutableSetStateFlow<Notification>()

    private val unreadNotificationsFlow = services
        .lastListViewModelFlow
        .filterIterable { it.viewModel.status == ChatMessageViewModel.Status.IncomingUnread }
        .filterNotEmpty()
        .flatMapLatest { chatMessagesWithStatus ->
            val unreadCountsFlow =
                services.unreadCountsFlow(chatMessagesWithStatus.map { it.message })
            services.getUsersByIdsFlow(chatMessagesWithStatus.map { it.message.secondId(currentUid) })
                .combine(unreadCountsFlow) { users, unreadCounts ->
                    val notifications = chatMessagesWithStatus
                        .mapNotNull { messageContainer ->
                            Notification.ChatMessage(
                                message = messageContainer.message,
                                senderName = users.firstOrNull {
                                    it.id == messageContainer.message.secondId(currentUid)
                                }?.fullName ?: return@mapNotNull null,
                                chatUnreadCount = unreadCounts[messageContainer.message.id]?.toInt() ?: 0,
                                totalUnreadCount = 0,
                            )
                        }
                    val totalUnreadCount = notifications.sumOf { it.chatUnreadCount }
                    notifications.map {
                        it.copy(totalUnreadCount = totalUnreadCount)
                    }
                }
        }

    private val readNotificationsFlow = currentNotificationsStateFlow
        .flatMapLatest { currentNotifications ->
            currentNotifications
                .filterIsInstance<Notification.ChatMessage>()
                .map { notification ->
                    services.getMessageByIdFlow(notification.message.id)
                        .print {
                            "currentNotificationsStateFlow getMessageByIdFlow $it"
                        }
                        .filter { it.viewModel.status == ChatMessageViewModel.Status.IncomingRead }
                        .map { notification }
                        .print {
                            "currentNotificationsStateFlow filter.map $it"
                        }
                }
                .flattenFlow()
                .print {
                    "currentNotificationsStateFlow flattenFlow $it"
                }
        }

    init {
        val job = Job()
        coroutineScope = parentCoroutineScope + job
        addCloseableChild(job.asCloseable())

        unreadNotificationsFlow
            .filterNotEmpty()
            .collectIn(coroutineScope) {
                it.forEach(::handleNotification)
                it.lastOrNull()?.totalUnreadCount?.let(notificationHelper::updateTotalUnreadCounter)
            }
        readNotificationsFlow
            .filterNotEmpty()
            .collectIn(coroutineScope) {
                Napier.d("readNotificationsFlow $it")
                it.forEach(notificationHelper::deleteNotification)
                currentNotificationsStateFlow.removeAll(it)
            }
    }

    fun handleRawNotification(rawNotification: RawNotification) {
        val notification = parseRawNotification(rawNotification)
        handleNotification(notification)
        notificationHelper.storeNotificationWithRaw(
            notification = notification,
            rawNotification = rawNotification
        )
    }

    fun clearAllNotifications() =
        notificationHelper.clearAllNotifications()

    internal fun parseRawNotification(rawNotification: RawNotification) =
        Json.decodeFromString<Notification>(
            notificationHelper.getNotificationPayloadString(rawNotification)
        )

    private fun handleNotification(notification: Notification) {
        when (notification) {
            is Notification.ChatMessage -> {
                if (notification.message.fromId == currentUid || notification.message.peerId != currentUid) {
                    Napier.e("unexpected $notification")
                    return
                }
                if (Platform.current == Platform.Platform.Android) {
                    notificationHelper.updateMessageNotification(notification)
                }
            }
        }
        currentNotificationsStateFlow.add(notification)
        Napier.i("currentNotificationsStateFlow ${currentNotificationsStateFlow.value}")
    }
}