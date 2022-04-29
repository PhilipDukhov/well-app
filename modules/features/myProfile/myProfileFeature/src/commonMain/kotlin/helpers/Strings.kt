package com.well.modules.features.myProfile.myProfileFeature.helpers

import com.well.modules.models.User
import com.well.modules.utils.viewUtils.GlobalStringsBase

internal object Strings: GlobalStringsBase() {
    const val logout = "Logout"
    const val becomeExpert = "Become an expert"
    const val requestConsultation = "Request consultation"
    const val about = "About"
    const val fullName = "Full name"
    const val credentials = "Credentials"
    const val emailAddress = "Email address"
    const val phoneNumber = "Phone number"
    const val yourCountry = "Your country"
    const val timeZone = "Time zone"
    const val more = "More"
    const val missionBio = "Mission/Bio"
    const val education = "Education"
    const val professionalMemberships = "Professional memberships"
    const val links = "Links"
    const val publications = "Publications"
    const val twitter = "Twitter"
    const val doximity = "Doximity"
    const val link = "link"
}

val User.Type.title: String
    get() {
        return when (this) {
            User.Type.Doctor
            -> "Doctor"
            User.Type.PendingExpert,
            User.Type.DeclinedExpert,
            -> "Doctor, expert verification pending"
            User.Type.Expert
            -> "Expert"
        }
    }