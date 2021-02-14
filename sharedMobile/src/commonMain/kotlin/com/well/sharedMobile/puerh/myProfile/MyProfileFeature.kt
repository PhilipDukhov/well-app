package com.well.sharedMobile.puerh.myProfile

import com.well.serverModels.User
import com.well.serverModels.formatters.currentTimeZoneIdentifier
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State.EditingStatus
import com.well.sharedMobile.puerh.πModels.NavigationBarModel
import com.well.sharedMobile.utils.ImageContainer
import com.well.sharedMobile.utils.profileImage
import com.well.utils.toSetOf
import com.well.utils.withEmptySet

object MyProfileFeature {
    fun initialState(
        isCurrent: Boolean,
        user: User,
    ) = user.run {
        if (user.initialized)
            this
        else
            copy(timeZoneIdentifier = timeZoneIdentifier ?: currentTimeZoneIdentifier())
    }.let {
        State(
            isCurrent = isCurrent,
            originalUser = it,
            user = it,
            editingStatus = if (!isCurrent || user.initialized) EditingStatus.Preview else EditingStatus.Editing,
        )
    }

    data class State internal constructor(
        val isCurrent: Boolean,
        internal val originalUser: User,
        internal val user: User,
        internal val newImage: ImageContainer? = null,
        internal val editingStatus: EditingStatus = EditingStatus.Preview,
    ) {
        internal val image = newImage ?: user.profileImage()
        val groups = listOf(
            UIGroup.Header(
                image = image,
                name = if (!user.initialized || editingStatus != EditingStatus.Preview) null else user.fullName,
                credentials = user.credentials,
                completeness = if (user.initialized) user.completeness else null,
                accountType = if (user.initialized) user.type else null,
                twitterLink = user.twitter,
            )
        ) + when (editingStatus) {
            EditingStatus.Preview -> user.previewGroups(credentialsNeeded = isCurrent)
            EditingStatus.Editing,
            EditingStatus.Uploading -> user.editingGroups()
        }
        private val valid = groups
            .mapNotNull { it as? UIGroup.Editing }
            .flatMap { it.fields }
            .all { it.content.valid() }
        val navigationBarModel = if (!isCurrent) null else NavigationBarModel(
            title = if (user.initialized) "My profile" else "Create new profile",
            leftItem = (if (user.initialized) when (editingStatus) {
                EditingStatus.Preview -> null
                EditingStatus.Editing,
                EditingStatus.Uploading -> {
                    NavigationBarModel.Item(
                        text = Strings.cancel,
                        enabled = editingStatus == EditingStatus.Editing,
                        msg = Msg.Back
                    )
                }
            } else null) ?: NavigationBarModel.Item(
                icon = NavigationBarModel.Item.Content.Icon.Icon.Back,
                enabled = editingStatus != EditingStatus.Uploading,
                msg = Msg.Back
            ),
            rightItem = when (editingStatus) {
                EditingStatus.Preview -> {
                    NavigationBarModel.Item(
                        text = "Edit",
                        msg = Msg.StartEditing
                    )
                }
                EditingStatus.Editing -> {
                    NavigationBarModel.Item(
                        text = if (user.initialized) "Save" else "Create",
                        enabled = valid && editingStatus == EditingStatus.Editing,
                        msg = Msg.FinishEditing
                    )
                }
                EditingStatus.Uploading -> {
                    NavigationBarModel.Item.activityIndicator()
                }
            },
        )

        internal enum class EditingStatus {
            Preview,
            Editing,
            Uploading,
        }
    }

    sealed class Msg {
        object StartEditing : Msg()
        object Back : Msg()
        object FinishEditing : Msg()
        object InitiateImageUpdate : Msg()
        object Call : Msg()
        data class OpenUrl(val url: String) : Msg()
        data class UpdateUser(val user: User) : Msg()
        data class UpdateImage(val imageContainer: ImageContainer?) : Msg()
        data class UserUploadFinished(val throwable: Throwable?) : Msg()
    }

    sealed class Eff {
        data class InitiateImageUpdate(val hasImage: Boolean) : Eff()
        data class OpenUrl(val url: String) : Eff()
        data class UploadUser(val user: User, val newProfileImage: ImageContainer?) : Eff()
        data class ShowError(val throwable: Throwable) : Eff()
        data class Call(val user: User) : Eff()
        object Pop : Eff()
        object InitializationFinished : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = when (msg) {
        is Msg.StartEditing -> {
            state.copy(editingStatus = EditingStatus.Editing).withEmptySet()
        }
        is Msg.OpenUrl -> {
            state toSetOf Eff.OpenUrl(msg.url)
        }
        is Msg.UpdateUser -> {
            state.copy(user = msg.user).withEmptySet()
        }
        is Msg.UpdateImage -> {
            state.copy(
                newImage = msg.imageContainer,
                user = state.user.copy(profileImageUrl = null),
            ).withEmptySet()
        }
        Msg.Back -> {
            if (state.user.initialized) {
                when (state.editingStatus) {
                    EditingStatus.Preview -> {
                        state toSetOf Eff.Pop
                    }
                    EditingStatus.Editing -> {
                        state.copy(
                            user = state.originalUser,
                            editingStatus = EditingStatus.Preview,
                        ).withEmptySet()
                    }
                    EditingStatus.Uploading -> state.withEmptySet()
                }
            } else {
                state toSetOf Eff.Pop
            }
        }
        Msg.FinishEditing -> {
            state.copy(
                editingStatus = EditingStatus.Uploading,
            ) toSetOf Eff.UploadUser(state.user, state.newImage)
        }
        is Msg.UserUploadFinished -> {
            if (msg.throwable != null) {
                state.copy(
                    editingStatus = EditingStatus.Editing,
                ) toSetOf Eff.ShowError(msg.throwable)
            } else {
                if (state.user.initialized) {
                    state.copy(
                        originalUser = state.user,
                        editingStatus = EditingStatus.Preview,
                    ).withEmptySet()
                } else {
                    state toSetOf Eff.InitializationFinished
                }
            }
        }
        Msg.InitiateImageUpdate -> {
            state toSetOf Eff.InitiateImageUpdate(state.image != null)
        }
        Msg.Call -> {
            state toSetOf Eff.Call(state.user)
        }
    }.also {
        println("$msg -> $it")
    }
}
