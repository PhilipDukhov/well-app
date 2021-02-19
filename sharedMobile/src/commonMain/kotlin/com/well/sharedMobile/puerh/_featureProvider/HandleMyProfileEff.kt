package com.well.sharedMobile.puerh._featureProvider

import com.well.napier.Napier
import com.well.sharedMobile.puerh._topLevel.*
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Eff
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.utils.dataStore.authToken
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.well.sharedMobile.puerh._topLevel.TopLevelFeature.Msg as TopLevelMsg

internal suspend fun FeatureProvider.handleMyProfileEff(
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
            networkManager.value.apply {
                var user = eff.user
                if (eff.newProfileImage != null) {
                    user = user.copy(profileImageUrl = uploadImage(user.id, eff.newProfileImage))
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
        is Eff.ShowError -> {
            listener(TopLevelMsg.ShowAlert(Alert.Throwable(eff.throwable)))
        }
        Eff.Pop -> {
            listener(TopLevelMsg.Pop)
        }
        Eff.InitializationFinished -> {
            loggedIn(nonInitializedUserToken.value, listener)
        }
        is Eff.Call -> {
            listener(TopLevelMsg.StartCall(eff.user))
        }
    }
}

private fun ((TopLevelMsg) -> Unit).invokeMyProfileMsg(msg: Msg) =
    invoke(TopLevelMsg.MyProfileMsg(msg))

private suspend fun FeatureProvider.pickSystemImage(listener: (TopLevelMsg) -> Unit) {
    val image = contextHelper.pickSystemImageSafe()
    if (image != null) {
        listener.invokeMyProfileMsg(Msg.UpdateImage(image))
    }
}