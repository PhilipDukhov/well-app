package com.well.sharedMobile.featureProvider

import com.well.modules.utils.viewUtils.Alert
import com.well.modules.utils.viewUtils.SuspendAction
import com.well.modules.utils.viewUtils.pickSystemImageSafe
import com.well.modules.utils.viewUtils.showSheetThreadSafe
import com.well.modules.features.experts.expertsFeature.ExpertsFeature
import com.well.modules.features.myProfile.MyProfileFeature.Eff
import com.well.modules.features.myProfile.MyProfileFeature.Msg
import com.well.modules.networking.userReadableDescription
import com.well.modules.utils.viewUtils.sharedImage.asByteArrayOptimizedForNetwork
import com.well.sharedMobile.TopLevelFeature.Msg as TopLevelMsg
import io.github.aakira.napier.Napier
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

internal suspend fun FeatureProviderImpl.handleMyProfileEff(
    eff: Eff,
    listener: (TopLevelMsg) -> Unit
) {
    when (eff) {
        is Eff.InitiateImageUpdate -> {
            if (eff.hasImage) {
                contextHelper.showSheetThreadSafe(
                    coroutineScope,
                    SuspendAction("Replace image") {
                        pickSystemImage(listener)
                    },
                    SuspendAction("Clear image") {
                        listener.invokeMyProfileMsg(Msg.UpdateImage(null))
                    },
                )
            } else {
                pickSystemImage(listener)
            }
        }
        is Eff.OpenUrl -> {
            MainScope().launch {
                contextHelper.openUrl(eff.url)
            }
        }
        is Eff.UploadUser -> {
            networkManager.apply {
                val user = eff.user.let { user ->
                    eff.newProfileImage?.let { newProfileImage ->
                        user.copy(
                            profileImageUrl = uploadProfilePicture(
                                user.id,
                                newProfileImage.asByteArrayOptimizedForNetwork()
                            )
                        )
                    } ?: user
                }
                listener.invokeMyProfileMsg(
                    Msg.UserUploadFinished(
                        try {
                            putUser(user)
                            null
                        } catch (t: Throwable) {
                            Napier.e("UploadUser $t")
                            t
                        }
                    )
                )
            }
        }
        is Eff.SetUserFavorite -> {
            networkManager.setFavorite(eff.setter)
        }
        is Eff.ShowError
        -> {
            listener(TopLevelMsg.ShowAlert(Alert.Error(eff.throwable, Throwable::userReadableDescription)))
        }
        is Eff.Back -> {
            listener(TopLevelMsg.Pop)
        }
        is Eff.InitializationFinished -> {
            loggedIn(nonInitializedAuthInfo.value, listener = listener)
        }
        is Eff.Call -> {
            listener(TopLevelMsg.StartCall(eff.user))
        }
        is Eff.Message -> {
            listener(TopLevelMsg.OpenUserChat(eff.uid))
        }
        is Eff.Logout -> {
            logOut(listener)
        }
        is Eff.BecomeExpert -> {
            networkManager.apply {
                requestBecomeExpert()
            }
        }
        is Eff.RatingRequest -> {
            networkManager.apply {
                rate(eff.ratingRequest)
                listener(TopLevelMsg.ExpertsMsg(ExpertsFeature.Msg.Reload))
            }
        }
        is Eff.AvailabilityEff -> TODO()
        is Eff.CloseConsultationRequest -> TODO()
        is Eff.RequestConsultationEff -> TODO()
    }
}

private fun ((TopLevelMsg) -> Unit).invokeMyProfileMsg(msg: Msg) =
    invoke(TopLevelMsg.MyProfileMsg(msg))

private suspend fun FeatureProviderImpl.pickSystemImage(listener: (TopLevelMsg) -> Unit) {
    val image = contextHelper.pickSystemImageSafe()
    if (image != null) {
        listener.invokeMyProfileMsg(Msg.UpdateImage(image.toImageContainer()))
    }
}