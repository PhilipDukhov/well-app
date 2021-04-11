package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.User
import com.well.sharedMobile.utils.SharedImage

sealed class UIGroup {
    companion object {
        fun Preview(vararg fields: UIPreviewField?) =
            fields.filterNotNull().let {
                if (it.isEmpty()) null else
                    Preview(it)
            }
    }

    data class Preview(
        val fields: List<UIPreviewField>,
    ) : UIGroup()

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
        val doximityLink: String?,
    ): UIGroup() {
        val initiateImageUpdateText = name ?: if (image != null) "Update image" else "Select image"
        val nameWithCredentials = name?.let {
            "$name${credentials?.let { ", $it" } ?: ""}"
        }
    }
}