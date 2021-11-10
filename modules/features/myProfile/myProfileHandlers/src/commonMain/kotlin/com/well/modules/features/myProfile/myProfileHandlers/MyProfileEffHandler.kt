package com.well.modules.features.myProfile.myProfileHandlers

import com.well.modules.atomic.AtomicCloseableRef
import com.well.modules.atomic.asCloseable
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Eff
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.Msg
import com.well.modules.features.myProfile.myProfileFeature.currentUserAvailability.CurrentUserAvailabilitiesListFeature
import com.well.modules.models.Availability
import com.well.modules.models.AvailabilityId
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.puerhBase.EffectHandler
import com.well.modules.utils.viewUtils.ContextHelper
import com.well.modules.utils.viewUtils.SuspendAction
import com.well.modules.utils.viewUtils.pickSystemImageSafe
import com.well.modules.utils.viewUtils.sharedImage.asByteArrayOptimizedForNetwork
import com.well.modules.utils.viewUtils.showSheetThreadSafe
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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
        val uploadProfilePicture: suspend (UserId, ByteArray) -> String,
        val showThrowableAlert: (Throwable) -> Unit,
        val onInitializationFinished: () -> Unit,
        val onPop: () -> Unit,
        val setFavorite: (FavoriteSetter) -> Unit,
        val onStartCall: (User) -> Unit,
        val onOpenUserChat: (UserId) -> Unit,
        val onLogout: () -> Unit,
        val requestBecomeExpert: () -> Unit,
        val onRatingRequest: (RatingRequest) -> Unit,
        val addAvailability: suspend (Availability) -> Unit,
        val removeAvailability: suspend (AvailabilityId) -> Unit,
        val updateAvailability: suspend (Availability) -> Unit,
        val book: suspend (Availability) -> Unit,
        val getAvailabilities: suspend () -> List<Availability>,
    )

    private var requestConsultationEffHandler by AtomicCloseableRef<RequestConsultationEffHandler>()

    init {
        Napier.d("init")
        addCloseableChild(
            coroutineScope.launch {
                services.userFlow
                    .map(Msg::RemoteUpdateUser)
                    .collect(::listener)
            }.asCloseable()
        )
    }

    override fun listener(msg: Msg) {
        super.listener(msg)
        Napier.d("listener $msg")
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
                if (requestConsultationEffHandler == null) {
                    requestConsultationEffHandler = RequestConsultationEffHandler(
                        services = RequestConsultationEffHandler.Services(
                            closeConsultationRequest = {
                                listener(Msg.CloseConsultationRequest)
                            },
                            book = services.book,
                            getAvailabilities = services.getAvailabilities
                        ),
                        coroutineScope = coroutineScope
                    )
                }
                requestConsultationEffHandler!!.handleEffect(eff.eff)
            }
        }
    }

    private suspend fun handleAvailabilityEff(eff: CurrentUserAvailabilitiesListFeature.Eff) {
        when (eff) {
            is CurrentUserAvailabilitiesListFeature.Eff.Add -> {
                services.addAvailability(eff.availability)
            }
            is CurrentUserAvailabilitiesListFeature.Eff.Remove -> {
                services.removeAvailability(eff.availabilityId)
            }
            is CurrentUserAvailabilitiesListFeature.Eff.Update -> {
                services.updateAvailability(eff.availability)
            }
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