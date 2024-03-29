package com.well.modules.features.myProfile.myProfileFeature.helpers

import com.well.modules.features.myProfile.myProfileFeature.MyProfileFeature
import com.well.modules.models.User
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
import com.well.modules.utils.viewUtils.UIEditingField
import com.well.modules.utils.viewUtils.sharedImage.SharedImage

sealed class UIGroup(val id: String) {
    companion object {
        fun Preview(vararg fields: UIPreviewField?) =
            fields.filterNotNull().let {
                ifTrueOrNull(it.isNotEmpty()) { Preview(it) }
            }
    }

    data class Preview(
        val fields: List<UIPreviewField>,
    ) : UIGroup(fields.joinToString { it.title })

    data class Editing(
        val title: String,
        val fields: List<UIEditingField<*, MyProfileFeature.Msg>>,
    ) : UIGroup(title)

    data class Header(
        val image: SharedImage?,
        val name: String?,
        val credentials: User.Credentials?,
        val favorite: Boolean,
        val reviewInfo: User.ReviewInfo,
        val completeness: Int?,
        val accountType: User.Type?,
        val twitterLink: String?,
        val doximityLink: String?,
    ) : UIGroup(name ?: "header") {
        val initiateImageUpdateText = name ?: if (image != null) "Update image" else "Select image"
        val nameWithCredentials = name?.let {
            "$name${credentials?.let { ", $it" } ?: ""}"
        }
    }
}