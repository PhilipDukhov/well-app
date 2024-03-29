package com.well.modules.features.myProfile.myProfileFeature

import com.well.modules.atomic.AtomicMutableMap
import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature.State.EditingStatus
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.AvailabilitiesCalendarFeature
import com.well.modules.features.myProfile.myProfileFeature.availabilitiesCalendar.RequestConsultationFeature
import com.well.modules.features.myProfile.myProfileFeature.helpers.Strings
import com.well.modules.features.myProfile.myProfileFeature.helpers.UIGroup
import com.well.modules.features.myProfile.myProfileFeature.helpers.editingGroups
import com.well.modules.features.myProfile.myProfileFeature.helpers.previewGroups
import com.well.modules.models.FavoriteSetter
import com.well.modules.models.Review
import com.well.modules.models.RatingRequest
import com.well.modules.models.User
import com.well.modules.puerhBase.toSetOf
import com.well.modules.puerhBase.withEmptySet
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
import com.well.modules.utils.kotlinUtils.spacedUppercaseName
import com.well.modules.utils.viewUtils.NavigationBarModel
import com.well.modules.utils.viewUtils.UrlUtil
import com.well.modules.utils.viewUtils.currentTimeZoneIdentifier
import com.well.modules.utils.viewUtils.sharedImage.ImageContainer
import com.well.modules.utils.viewUtils.sharedImage.profileImage
import com.well.modules.utils.viewUtils.urlIfValidOrNull
import io.github.aakira.napier.Napier

object MyProfileFeature {
    enum class ProfileTab {
        ProfileInformation,
        Availability,
        Settings,
        ;

        val title get() = spacedUppercaseName()
    }

    fun initialState(
        isCurrent: Boolean,
        uid: User.Id,
    ) = initialState(
        isCurrent = isCurrent,
        uid = uid,
        user = null,
    )

    fun initialState(
        isCurrent: Boolean,
        user: User,
    ) = initialState(
        isCurrent = isCurrent,
        uid = user.id,
        user = user,
    )

    private fun initialState(
        isCurrent: Boolean,
        uid: User.Id,
        user: User? = null,
    ) = user?.let {
        if (user.initialized)
            user
        else
            user.copy(
                timeZoneIdentifier = user.timeZoneIdentifier ?: currentTimeZoneIdentifier(),
            )
    }.let {
        State(
            isCurrent = isCurrent,
            uid = uid,
            originalUser = it,
            user = it,
            editingStatus = if (!isCurrent || user?.initialized != false) EditingStatus.Preview else EditingStatus.Editing,
            availabilityState = if (isCurrent && user?.initialized != false) AvailabilitiesCalendarFeature.State() else null,
            settingsState = if (isCurrent && user?.initialized != false) SettingsFeature.State() else null,
            hasAvailableAvailabilities = if (isCurrent) null else false
        )
    }

    data class State internal constructor(
        val isCurrent: Boolean,
        val uid: User.Id,
        internal val originalUser: User? = null,
        val user: User? = null,
        internal val newImage: ImageContainer? = null,
        val editingStatus: EditingStatus = EditingStatus.Preview,
        val availabilityState: AvailabilitiesCalendarFeature.State? = null,
        val requestConsultationState: RequestConsultationFeature.State? = null,
        val settingsState: SettingsFeature.State? = null,
        val hasAvailableAvailabilities: Boolean?,
    ) {
        val tabs =
            if (editingStatus != EditingStatus.Preview || availabilityState == null)
                listOf(ProfileTab.ProfileInformation)
            else
                ProfileTab.values().toList()

        val maxRatingCharacters = 150
        val loaded = user != null
        internal val image = newImage ?: user?.profileImage()
        val groups = if (user != null) {
            listOf(
                UIGroup.Header(
                    image = image,
                    name = ifTrueOrNull(user.initialized && editingStatus == EditingStatus.Preview) { user.fullName },
                    credentials = user.credentials,
                    favorite = user.favorite,
                    reviewInfo = user.reviewInfo,
                    completeness = if (user.initialized) user.completeness else null,
                    accountType = if (user.initialized) user.type else null,
                    twitterLink = user.twitter?.let(UrlUtil::urlIfValidOrNull),
                    doximityLink = user.doximity?.let(UrlUtil::urlIfValidOrNull),
                ),
            ) + when (editingStatus) {
                EditingStatus.Preview -> user.previewGroups(isCurrent, hasAvailableAvailabilities)
                EditingStatus.Editing,
                EditingStatus.Uploading,
                -> user.editingGroups()
            }
        } else {
            emptyList()
        }
        private val valid = groups
            .mapNotNull { it as? UIGroup.Editing }
            .flatMap { it.fields }
            .all { it.content.valid() }

        private val cachedNavigationBarModels = AtomicMutableMap<ProfileTab, NavigationBarModel<Msg>?>()
        fun navigationBarModelForTab(tab: ProfileTab): NavigationBarModel<Msg>? {
            if (!isCurrent) return null
            if (cachedNavigationBarModels.containsKey(tab)) {
                return cachedNavigationBarModels[tab]
            }
            val result = NavigationBarModel(
                title = if (user?.initialized == false) "Create new profile" else "My profile",
                leftItem = run {
                    (if (user?.initialized == true) when (editingStatus) {
                        EditingStatus.Preview -> if (isCurrent) return@run null else null
                        EditingStatus.Editing,
                        EditingStatus.Uploading,
                        -> {
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
                    )
                },
                rightItem = when (tab) {
                    ProfileTab.ProfileInformation -> {
                        when (editingStatus) {
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
                        }
                    }
                    ProfileTab.Settings,
                    ProfileTab.Availability,
                    -> {
                        null
                    }
                },
            )
            cachedNavigationBarModels[tab] = result
            return result
        }

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
        class OpenUrl(val url: String) : Msg()
        class RemoteUpdateUser(val user: User) : Msg()
        class UpdateUser(val user: User) : Msg()
        class UpdateImage(val imageContainer: ImageContainer?) : Msg()
        class UserUploadFinished(val exception: Exception?) : Msg()
        class UpdateHasAvailableAvailabilities(val hasAvailableAvailabilities: Boolean) : Msg()
        class Rate(val review: Review) : Msg()
        class AvailabilityMsg(val msg: AvailabilitiesCalendarFeature.Msg) : Msg()
        object RequestConsultation : Msg()
        object CloseConsultationRequest : Msg()
        class RequestConsultationMsg(val msg: RequestConsultationFeature.Msg) : Msg()
        class SettingsMsg(val msg: SettingsFeature.Msg) : Msg()
    }

    sealed interface Eff {
        class InitiateImageUpdate(val hasImage: Boolean) : Eff
        class OpenUrl(val url: String) : Eff
        data class UploadUser(
            val user: User,
            val newProfileImage: ImageContainer?,
        ) : Eff

        class ShowError(val exception: Exception) : Eff
        class Call(val user: User) : Eff
        class Message(val uid: User.Id) : Eff
        class RatingRequest(val ratingRequest: com.well.modules.models.RatingRequest) : Eff

        object Back : Eff
        object InitializationFinished : Eff
        object BecomeExpert : Eff
        class SetUserFavorite(val setter: FavoriteSetter) : Eff
        class AvailabilityEff(val eff: AvailabilitiesCalendarFeature.Eff) : Eff
        class RequestConsultationEff(val eff: RequestConsultationFeature.Eff) : Eff
        class SettingsEff(val eff: SettingsFeature.Eff) : Eff
        object CloseConsultationRequest : Eff
    }

    fun reducer(
        msg: Msg,
        state: State,
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
                                return@eff Eff.Back
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
                        return@eff Eff.Back
                    }
                }
                is Msg.FinishEditing -> {
                    return@reducer state.copy(
                        editingStatus = EditingStatus.Uploading,
                    ) toSetOf Eff.UploadUser(state.user!!, state.newImage)
                }
                is Msg.UserUploadFinished -> {
                    if (msg.exception != null) {
                        return@reducer state.copy(
                            editingStatus = EditingStatus.Editing,
                        ) toSetOf Eff.ShowError(msg.exception)
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
                    return@eff Eff.Message(state.uid)
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
                            reviewInfo = state.user.reviewInfo.copy(
                                currentUserReview = msg.review,
                            ),
                        )
                    ) toSetOf Eff.RatingRequest(
                        ratingRequest = RatingRequest(
                            uid = state.user.id,
                            review = msg.review,
                        )
                    )
                }
                is Msg.AvailabilityMsg -> {
                    if (state.availabilityState == null) {
                        Napier.wtf("AvailabilityMsg ${msg.msg} unexpected: $state")
                        return@state state
                    }
                    val (childState, effs) = AvailabilitiesCalendarFeature.reducer(
                        msg = msg.msg,
                        state = state.availabilityState,
                    )
                    return@reducer state.copy(
                        availabilityState = childState
                    ) to effs.mapTo(mutableSetOf(), Eff::AvailabilityEff)
                }
                Msg.RequestConsultation -> {
                    if (state.isCurrent || state.user?.initialized != true) {
                        Napier.wtf("$msg unexpected: $state")
                        return@state state
                    }
                    return@reducer state.reduceConsultation(
                        RequestConsultationFeature.initial()
                    )
                }
                is Msg.RequestConsultationMsg -> {
                    if (state.requestConsultationState == null) {
                        Napier.wtf("RequestConsultationMsg ${msg.msg} unexpected: $state")
                        return@state state
                    }
                    return@reducer state.reduceConsultation(
                        RequestConsultationFeature.reducer(
                            msg = msg.msg,
                            state = state.requestConsultationState,
                        )
                    )
                }
                Msg.CloseConsultationRequest -> {
                    return@reducer state.copy(
                        requestConsultationState = null,
                    ) toSetOf Eff.CloseConsultationRequest
                }
                is Msg.UpdateHasAvailableAvailabilities -> {
                    return@state state.copy(
                        hasAvailableAvailabilities = msg.hasAvailableAvailabilities,
                    )
                }
                is Msg.SettingsMsg -> {
                    val reducerResult = SettingsFeature.reducer(msg.msg, state.settingsState ?: return@state state)
                    return@reducer state.copy(
                        settingsState = reducerResult.first
                    ) to reducerResult.second.mapTo(mutableSetOf(), Eff::SettingsEff)
                }
            }
        })
    }.withEmptySet()

    private fun State.reduceConsultation(
        reducerResult: Pair<RequestConsultationFeature.State, Set<RequestConsultationFeature.Eff>>,
    ) = copy(
        requestConsultationState = reducerResult.first
    ) to reducerResult.second.mapTo(mutableSetOf(), Eff::RequestConsultationEff)
}