package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State.EditingStatus
import com.well.sharedMobile.puerh.πModels.NavigationBarModel
import com.well.sharedMobile.utils.ImageContainer
import com.well.sharedMobile.utils.profileImage
import com.well.modules.utils.base.UrlUtil
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.utils.currentTimeZoneIdentifier

object MyProfileFeature {
    fun testState() = initialState(
        true,
        User(
            id = 1,
            initialized = true,
            fullName = "12",
            profileImageUrl = "https://i.imgur.com/StXm8nf.jpg",
            type = User.Type.Doctor,
            phoneNumber = "+380686042511",
            timeZoneIdentifier = "America/Los_Angeles",
            credentials = User.Credentials.MD,
            academicRank = User.AcademicRank.AssistantProfessor,
            languages = setOf(User.Language.English, User.Language.Russian),
            bio = "LA, CaliforniaLA, CaliforniaLA, CaliforniaLA, CaliforniaLA, California",
            education = "LA, CaliforniaLA, California",
            professionalMemberships = "CaliforniaLA, California",
            publications = "CaliforniaLA, California",
            twitter = "CaliforniaLA, California",
            doximity = "CaliforniaLA, California",
            skills = setOf(
                User.Skill.BPH,
                User.Skill.RoboticCystectomy,
                User.Skill.RoboticUrinaryReconstructionSurgery,
                User.Skill.RoboticRenalSurgery,
                User.Skill.PercutaneousNephrolithotomy
            ),
        )
    ).copy(editingStatus = EditingStatus.Editing)

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
        val editingStatus: EditingStatus = EditingStatus.Preview,
    ) {
        internal val image = newImage ?: user.profileImage()
        val groups = listOf(
            UIGroup.Header(
                image = image,
                name = if (!user.initialized || editingStatus != EditingStatus.Preview) null else user.fullName,
                credentials = user.credentials,
                completeness = if (user.initialized) user.completeness else null,
                accountType = if (user.initialized) user.type else null,
                twitterLink = user.twitter?.let { if (!UrlUtil.isValidUrl(it)) null else it },
                doximityLink = user.doximity?.let { if (!UrlUtil.isValidUrl(it)) null else it },
            )
        ) + when (editingStatus) {
            EditingStatus.Preview -> user.previewGroups(isCurrent = isCurrent)
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
                EditingStatus.Preview -> {
                    NavigationBarModel.Item(
                        text = Strings.logout,
                        msg = Msg.OnLogout,
                    )
                }
                EditingStatus.Editing,
                EditingStatus.Uploading -> {
                    NavigationBarModel.Item(
                        text = Strings.cancel,
                        enabled = editingStatus == EditingStatus.Editing,
                        msg = Msg.Back,
                    )
                }
            } else null) ?: NavigationBarModel.Item(
                icon = NavigationBarModel.Item.Content.Icon.Icon.Back,
                enabled = editingStatus != EditingStatus.Uploading,
                msg = Msg.Back,
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

        enum class EditingStatus {
            Preview,
            Editing,
            Uploading,
        }
    }

    sealed class Msg {
        object StartEditing : Msg()
        object BecomeExpert : Msg()
        object Back : Msg()
        object FinishEditing : Msg()
        object InitiateImageUpdate : Msg()
        object Call : Msg()
        data class OpenUrl(val url: String) : Msg()
        data class UpdateUser(val user: User) : Msg()
        data class UpdateImage(val imageContainer: ImageContainer?) : Msg()
        data class UserUploadFinished(val throwable: Throwable?) : Msg()
        object OnLogout : Msg()
    }

    sealed class Eff {
        data class InitiateImageUpdate(val hasImage: Boolean) : Eff()
        data class OpenUrl(val url: String) : Eff()
        data class UploadUser(
            val user: User,
            val newProfileImage: ImageContainer?
        ) : Eff()

        data class ShowError(val throwable: Throwable) : Eff()
        data class Call(val user: User) : Eff()
        object Pop : Eff()
        object InitializationFinished : Eff()
        object Logout : Eff()
        object BecomeExpert : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.StartEditing -> {
                    return@state state.copy(editingStatus = EditingStatus.Editing)
                }
                is Msg.OpenUrl -> {
                    return@eff Eff.OpenUrl(msg.url)
                }
                is Msg.UpdateUser -> {
                    return@state state.copy(user = msg.user)
                }
                is Msg.UpdateImage -> {
                    return@state state.copy(
                        newImage = msg.imageContainer,
                        user = state.user.copy(profileImageUrl = null),
                    )
                }
                Msg.Back -> {
                    if (state.user.initialized) {
                        when (state.editingStatus) {
                            EditingStatus.Preview -> {
                                return@eff Eff.Pop
                            }
                            EditingStatus.Editing -> {
                                return@state state.copy(
                                    user = state.originalUser,
                                    editingStatus = EditingStatus.Preview,
                                )
                            }
                            EditingStatus.Uploading -> return@state state
                        }
                    } else {
                        return@eff Eff.Pop
                    }
                }
                Msg.FinishEditing -> {
                    return@reducer state.copy(
                        editingStatus = EditingStatus.Uploading,
                    ) toSetOf Eff.UploadUser(state.user, state.newImage)
                }
                is Msg.UserUploadFinished -> {
                    if (msg.throwable != null) {
                        return@reducer state.copy(
                            editingStatus = EditingStatus.Editing,
                        ) toSetOf Eff.ShowError(msg.throwable)
                    } else {
                        if (state.user.initialized) {
                            return@state state.copy(
                                originalUser = state.user,
                                editingStatus = EditingStatus.Preview,
                            )
                        } else {
                            return@eff Eff.InitializationFinished
                        }
                    }
                }
                Msg.InitiateImageUpdate -> {
                    return@eff Eff.InitiateImageUpdate(state.image != null)
                }
                Msg.Call -> {
                    return@eff Eff.Call(state.user)
                }
                Msg.OnLogout -> {
                    return@eff Eff.Logout
                }
                Msg.BecomeExpert -> {
                    val user = state.originalUser.copy(type = User.Type.PendingExpert)
                    return@reducer state.copy(
                        user = user,
                        originalUser = user,
                    ) toSetOf Eff.BecomeExpert
                }
            }
        })
    }.withEmptySet()
}
