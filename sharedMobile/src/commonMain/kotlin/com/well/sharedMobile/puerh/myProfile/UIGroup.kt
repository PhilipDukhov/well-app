package com.well.sharedMobile.puerh.myProfile

import com.well.serverModels.User
import com.well.sharedMobile.utils.SharedImage

sealed class UIGroup {
    data class Preview(
        val fields: List<UIPreviewField>,
    ) : UIGroup() {
        constructor(vararg fields: UIPreviewField?) : this(fields.filterNotNull())
    }

    data class Editing(
        val title: String,
        val fields: List<UIEditingField<*>>,
    ) : UIGroup()

    data class Header(
        val image: SharedImage?,
        val name: String?,
        val credentials: User.Credentials?,
        val completeness: Int?,
        val accountType: User.Type?,
        val twitterLink: String?,
    ): UIGroup() {
        val initiateImageUpdateText = name ?: if (image != null) "Update image" else "Select image"
        val nameWithCredentials = name?.let {
            "$name${credentials?.let { ", $it" } ?: ""}"
        }
    }
}