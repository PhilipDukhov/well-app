package com.well.modules.features.myProfile.myProfileHandlers

import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.AtomicMutableList
import com.well.modules.atomic.asCloseable
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Eff
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Msg
import com.well.modules.models.Availability
import com.well.modules.models.AvailabilityId
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.flowUtils.collectIn
import com.well.modules.utils.viewUtils.ContextHelper
import com.well.modules.utils.viewUtils.SuspendAction
import com.well.modules.utils.viewUtils.pickSystemImageSafe
import com.well.modules.utils.viewUtils.sharedImage.asByteArrayOptimizedForNetwork
import com.well.modules.utils.viewUtils.showSheetThreadSafe
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature.Eff as AvailabilitiesCalendarEff
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature.Msg as AvailabilitiesCalendarMsg
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MyProfileEffHandler(
    private val contextHelper: ContextHelper,
    private val services: Services,
    coroutineScope: CoroutineScope,
) : EffectHandler<Eff, Msg>(coroutineScope) {
    data class Services(
        val userFlow: Flow<User>,
        val putUser: suspend (User) -> Unit,
        val uploadProfilePicture: suspend (User.Id, ByteArray) -> String,
        val showThrowableAlert: (Throwable) -> Unit,
        val onInitializationFinished: () -> Unit,
        val onPop: () -> Unit,
        val setFavorite: (FavoriteSetter) -> Unit,
        val onStartCall: (User) -> Unit,
        val onOpenUserChat: (User.Id) -> Unit,
        val onLogout: () -> Unit,
        val requestBecomeExpert: () -> Unit,
        val onRatingRequest: (RatingRequest) -> Unit,

        val getCurrentUserAvailabilities: suspend () -> List<Availability>,
        val addAvailability: suspend (Availability) -> Availability,
        val updateAvailability: suspend (Availability) -> Availability,
        val removeAvailability: suspend (AvailabilityId) -> Unit,

        val hasAvailableAvailabilities: suspend () -> Boolean,
        val book: suspend (Availability) -> Unit,
        val getUserAvailabilitiesToBook: suspend () -> List<Availability>,
    )

    private var requestConsultationEffHandler by AtomicCloseableRef<RequestConsultationEffHandler>()

    init {
        listOf(
            coroutineScope.launch {
                if (services.hasAvailableAvailabilities()) {
                    listener(Msg.UpdateHasAvailableAvailabilities(true))
                }
            },
            services.userFlow
                .map(Msg::RemoteUpdateUser)
                .collectIn(coroutineScope, action = ::listener),
        ).map(Job::asCloseable).forEach(::addCloseableChild)
    }

    override fun listener(msg: Msg) {
        super.listener(msg)
        Napier.d("listener $msg")
    }

    private fun availabilitiesCalendarListener(msg: AvailabilitiesCalendarMsg) {
        listener(Msg.AvailabilityMsg(msg))
    }

    override suspend fun processEffect(eff: Eff) {
        Napier.d("processEffect $this $eff")
        when (eff) {
            is Eff.InitiateImageUpdate -> {
                coroutineScope.launch {
                    initiateImageUpdate(eff)
                }
            }
            is Eff.OpenUrl -> {
                MainScope().launch {
                    contextHelper.openUrl(eff.url)
                }
            }
            is Eff.UploadUser -> {
                coroutineScope.launch {
                    uploadUser(eff)
                }
            }
            is Eff.SetUserFavorite -> {
                services.setFavorite(eff.setter)
            }
            is Eff.ShowError -> {
                services.showThrowableAlert(eff.throwable)
            }
            is Eff.Back -> {
                services.onPop()
            }
            is Eff.InitializationFinished -> {
                services.onInitializationFinished
            }
            is Eff.Call -> {
                services.onStartCall(eff.user)
            }
            is Eff.Message -> {
                services.onOpenUserChat(eff.uid)
            }
            is Eff.Logout -> {
                services.onLogout()
            }
            is Eff.BecomeExpert -> {
                services.requestBecomeExpert()
            }
            is Eff.RatingRequest -> {
                services.onRatingRequest(eff.ratingRequest)
            }
            is Eff.AvailabilityEff -> {
                handleAvailabilityEff(eff.eff)
            }
            is Eff.CloseConsultationRequest -> {
                requestConsultationEffHandler = null
            }
            is Eff.RequestConsultationEff -> {
                val handler = requestConsultationEffHandler ?: RequestConsultationEffHandler(
                    services = RequestConsultationEffHandler.Services(
                        closeConsultationRequest = {
                            listener(Msg.CloseConsultationRequest)
                        },
                        book = services.book,
                        getAvailabilities = services.getUserAvailabilitiesToBook,
                        gotEmptyAvailabilities = {
                            listener(Msg.UpdateHasAvailableAvailabilities(false))
                        }
                    ),
                    coroutineScope = coroutineScope
                ).also {
                    it.setListener {
                        listener(Msg.RequestConsultationMsg(it))
                    }
                    requestConsultationEffHandler = it
                }
                handler.handleEffect(eff.eff)
            }
        }
    }

    private val currentUserAvailabilities = AtomicMutableList<Availability>()

    private suspend fun handleAvailabilityEff(eff: AvailabilitiesCalendarEff) {
        println("SetProcessing(true) $eff")
        availabilitiesCalendarListener(AvailabilitiesCalendarMsg.SetProcessing(true))
        try {
            when (eff) {
                is AvailabilitiesCalendarEff.Add -> {
                    val newAvailability = services.addAvailability(eff.availability)
                    currentUserAvailabilities.add(newAvailability)
                }
                is AvailabilitiesCalendarEff.Remove -> {
                    services.removeAvailability(eff.availabilityId)
                    currentUserAvailabilities.removeAll { it.id == eff.availabilityId }
                }
                is AvailabilitiesCalendarEff.Update -> {
                    val newAvailability = services.updateAvailability(eff.availability)
                    currentUserAvailabilities.apply {
                        removeAll { it.id == eff.availability.id }
                        add(newAvailability)
                    }
                }
                is AvailabilitiesCalendarEff.RequestAvailabilities -> {
                    val availabilities = services.getCurrentUserAvailabilities()
                    currentUserAvailabilities.apply {
                        clear()
                        addAll(availabilities)
                    }
                }
            }
            availabilitiesCalendarListener(
                AvailabilitiesCalendarMsg.SetAvailabilities(
                    currentUserAvailabilities
                )
            )
        } catch (t: Throwable) {
            availabilitiesCalendarListener(
                AvailabilitiesCalendarMsg.RequestFailed(
                    t.message ?: t.toString()
                )
            )
        } finally {
            println("SetProcessing(false) $eff")
            availabilitiesCalendarListener(AvailabilitiesCalendarMsg.SetProcessing(false))
        }
    }

    private suspend fun uploadUser(eff: Eff.UploadUser) {
        val user = eff.user.let { user ->
            eff.newProfileImage?.let { newProfileImage ->
                val profileImageUrl = services.uploadProfilePicture(
                    user.id,
                    newProfileImage.asByteArrayOptimizedForNetwork()
                )
                user.copy(profileImageUrl = profileImageUrl)
            } ?: user
        }
        val userUploadError = try {
            services.putUser(user)
            null
        } catch (t: Throwable) {
            Napier.e("UploadUser $t")
            t
        }
        listener(Msg.UserUploadFinished(userUploadError))
    }

    private suspend fun initiateImageUpdate(eff: Eff.InitiateImageUpdate) {
        if (eff.hasImage) {
            contextHelper.showSheetThreadSafe(
                SuspendAction("Replace image") {
                    pickSystemImage()
                },
                SuspendAction("Clear image") {
                    listener(Msg.UpdateImage(null))
                },
            )
        } else {
            pickSystemImage()
        }
    }

    private suspend fun pickSystemImage() {
        val image = contextHelper.pickSystemImageSafe()
        if (image != null) {
            listener(Msg.UpdateImage(image.toImageContainer()))
        }
    }
}