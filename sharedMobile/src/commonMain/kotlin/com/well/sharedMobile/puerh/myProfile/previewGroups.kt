package com.well.sharedMobile.puerh.myProfile

import com.well.serverModels.Date
import com.well.serverModels.User
import com.well.serverModels.formatters.DateFormatter
import com.well.serverModels.formatters.format
import com.well.serverModels.spacedUppercaseName

internal fun User.previewGroups(credentialsNeeded: Boolean) = listOf(
    UIGroup.Preview(
        if (credentialsNeeded) credentials?.name?.let {
            UIPreviewField(title = Strings.credentials, text = it)
        } else null,
        academicRank?.spacedUppercaseName()?.let {
            UIPreviewField(title = Strings.academicRank, text = it)
        },
        if (skills.isEmpty()) null else
            UIPreviewField(
                title = Strings.skillsExpertise,
                set = skills
                    .map { it.spacedUppercaseName() }
                    .sorted()
            ),
    ),
    UIGroup.Preview(
        email?.let {
            UIPreviewField(title = Strings.emailAddress, text = it)
        },
        phoneNumber?.let {
            UIPreviewField(title = Strings.phoneNumber, text = it)
        },
    ),
    UIGroup.Preview(
        location?.let {
            UIPreviewField(
                title = Strings.location,
                text = it,
                icon = UIPreviewField.Icon.Location
            )
        },
        timeZoneIdentifier?.let {
            UIPreviewField(
                title = Strings.timeZone,
                text = DateFormatter.format(Date(), it)
            )
        },
    ),
    UIGroup.Preview(
        if (languages.isEmpty()) null else
            UIPreviewField(
                title = Strings.languagesSpoken,
                set = languages
                    .map { it.spacedUppercaseName() }
                    .sorted()
            ),
    ),
    UIGroup.Preview(
        bio?.let {
            UIPreviewField(title = Strings.missionBio, text = it)
        },
    ),
    UIGroup.Preview(
        education?.let {
            UIPreviewField(title = Strings.education, text = it)
        },
        professionalMemberships?.let {
            UIPreviewField(title = Strings.professionalMemberships, text = it)
        },
    ),
    UIGroup.Preview(
        publications?.let {
            UIPreviewField(
                title = Strings.publications,
                text = it,
                icon = UIPreviewField.Icon.Publications,
                isLink = true
            )
        },
        twitter?.let {
            UIPreviewField(
                title = Strings.twitter,
                text = it,
                icon = UIPreviewField.Icon.Twitter,
                isLink = true
            )
        },
        doximity?.let {
            UIPreviewField(
                title = Strings.doximity,
                text = it,
                icon = UIPreviewField.Icon.Doximity,
                isLink = true
            )
        },
    ),
).filter { it.fields.isNotEmpty() }
