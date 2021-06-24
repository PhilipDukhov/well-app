package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.FavoriteSetter
import com.well.modules.models.Rating
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.models.UserId
import com.well.modules.utils.UrlUtil
import com.well.modules.utils.toSetOf
import com.well.modules.utils.withEmptySet
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.State.EditingStatus
import com.well.sharedMobile.puerh.πModels.NavigationBarModel
import com.well.modules.utils.sharedImage.ImageContainer
import com.well.sharedMobile.utils.currentTimeZoneIdentifier
import com.well.modules.utils.sharedImage.profileImage

object MyProfileFeature {
    fun testState() = initialState(
        true,
        User(
            id = 1,
            initialized = true,
            lastEdited = 0.0,
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
            ratingInfo = User.RatingInfo(
                count = 0,
                average = 0.0,
            ),
        ),
    ).copy(editingStatus = EditingStatus.Editing)

    fun initialState(
        isCurrent: Boolean,
        uid: UserId,
    ) = State(
        isCurrent = isCurrent,
        uid = uid,
    )

    fun initialState(
        isCurrent: Boolean,
        user: User,
    ) = user.run {
        if (user.initialized)
            this
        else
            copy(
                timeZoneIdentifier = timeZoneIdentifier ?: currentTimeZoneIdentifier(),
            )
    }.let {
        State(
            isCurrent = isCurrent,
            uid = user.id,
            originalUser = it,
            user = it,
            editingStatus = if (!isCurrent || user.initialized) EditingStatus.Preview else EditingStatus.Editing,
        )
    }

    data class State internal constructor(
        val isCurrent: Boolean,
        internal val uid: UserId,
        internal val originalUser: User? = null,
        val user: User? = null,
        internal val newImage: ImageContainer? = null,
        val editingStatus: EditingStatus = EditingStatus.Preview,
    ) {
        init {
            println("State $uid $user $originalUser")
        }
        val loaded = user != null
        internal val image = newImage ?: user?.profileImage()
        val groups = if (user != null) {
            listOf(
                UIGroup.Header(
                    image = image,
                    name = if (!user.initialized || editingStatus != EditingStatus.Preview) null else user.fullName,
                    credentials = user.credentials,
                    favorite = user.favorite,
                    ratingInfo = user.ratingInfo,
                    completeness = if (user.initialized) user.completeness else null,
                    accountType = if (user.initialized) user.type else null,
                    twitterLink = user.twitter?.let { if (!UrlUtil.isValidUrl(it)) null else it },
                    doximityLink = user.doximity?.let { if (!UrlUtil.isValidUrl(it)) null else it },
                ),
            ) + when (editingStatus) {
                EditingStatus.Preview -> user.previewGroups(isCurrent = isCurrent)
                EditingStatus.Editing,
                EditingStatus.Uploading -> user.editingGroups()
            }
        } else {
            emptyList()
        }
        private val valid = groups
            .mapNotNull { it as? UIGroup.Editing }
            .flatMap { it.fields }
            .all { it.content.valid() }
        val navigationBarModel = if (!isCurrent) null else NavigationBarModel(
            title = if (user?.initialized == false) "Create new profile" else "My profile",
            leftItem = (if (user?.initialized == true) when (editingStatus) {
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
                        text = if (user?.initialized == true) "Save" else "Create",
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
        object Message : Msg()
        object ToggleFavorite : Msg()
        data class OpenUrl(val url: String) : Msg()
        data class RemoteUpdateUser(val user: User) : Msg()
        data class UpdateUser(val user: User) : Msg()
        data class UpdateImage(val imageContainer: ImageContainer?) : Msg()
        data class UserUploadFinished(val throwable: Throwable?) : Msg()
        data class Rate(val rating: Rating) : Msg()
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
        data class Message(val uid: UserId) : Eff()
        data class RatingRequest(val ratingRequest: com.well.modules.models.RatingRequest) :
            Eff()

        object Pop : Eff()
        object InitializationFinished : Eff()
        object Logout : Eff()
        object BecomeExpert : Eff()
        data class SetUserFavorite(val setter: FavoriteSetter) : Eff()
    }

    fun reducer(
        msg: Msg,
        state: State
    ): Pair<State, Set<Eff>> = run state@{
        return@reducer state toSetOf (run eff@{
            when (msg) {
                is Msg.RemoteUpdateUser -> {
                    return@state state.copy(user = msg.user, originalUser = msg.user)
                }
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
                        user = state.user!!.copy(profileImageUrl = null),
                    )
                }
                is Msg.ToggleFavorite -> {
                    val newUser = state.user!!.copy(favorite = !state.user.favorite)
                    return@reducer state.copy(
                        user = newUser,
                    ) toSetOf Eff.SetUserFavorite(FavoriteSetter(newUser.favorite, newUser.id))
                }
                is Msg.Back -> {
                    if (state.user?.initialized == true) {
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
                is Msg.FinishEditing -> {
                    return@reducer state.copy(
                        editingStatus = EditingStatus.Uploading,
                    ) toSetOf Eff.UploadUser(state.user!!, state.newImage)
                }
                is Msg.UserUploadFinished -> {
                    if (msg.throwable != null) {
                        return@reducer state.copy(
                            editingStatus = EditingStatus.Editing,
                        ) toSetOf Eff.ShowError(msg.throwable)
                    } else {
                        if (state.user!!.initialized) {
                            return@state state.copy(
                                originalUser = state.user,
                                editingStatus = EditingStatus.Preview,
                            )
                        } else {
                            return@eff Eff.InitializationFinished
                        }
                    }
                }
                is Msg.InitiateImageUpdate -> {
                    return@eff Eff.InitiateImageUpdate(state.image != null)
                }
                is Msg.Call -> {
                    return@eff Eff.Call(state.user!!)
                }
                is Msg.Message -> {
                    println("reduce Msg.Message ${state.uid} $state")
                    return@eff Eff.Message(state.uid)
                }
                is Msg.OnLogout -> {
                    return@eff Eff.Logout
                }
                is Msg.BecomeExpert -> {
                    val user = state.originalUser!!.copy(type = User.Type.PendingExpert)
                    return@reducer state.copy(
                        user = user,
                    ) toSetOf Eff.BecomeExpert
                }
                is Msg.Rate -> {
                    return@reducer state.copy(
                        user = state.user!!.copy(
                            ratingInfo = state.user.ratingInfo.copy(currentUserRating = msg.rating),
                        )
                    ) toSetOf Eff.RatingRequest(
                        ratingRequest = RatingRequest(
                            uid = state.user.id,
                            rating = msg.rating,
                        )
                    )
                }
            }
        })
    }.withEmptySet()
}
