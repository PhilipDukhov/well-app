package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.Date
import com.well.modules.models.User
import com.well.modules.models.formatters.DateFormatter
import com.well.modules.models.formatters.format
import com.well.modules.models.spacedUppercaseName

internal fun User.previewGroups(isCurrent: Boolean) = listOfNotNull(
    UIGroup.Preview(
        if (!isCurrent) null else
            credentials?.name?.let {
                UIPreviewField(title = Strings.credentials, text = it)
            },
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
    if (!isCurrent) null else
        UIGroup.Preview(
            email?.let {
                UIPreviewField(title = Strings.emailAddress, text = it)
            },
            phoneNumber?.let {
                UIPreviewField(title = Strings.phoneNumber, text = it)
            },
        ),
    UIGroup.Preview(
        countryCode?.let {
            UIPreviewField(
                title = Strings.country,
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
        if (!isCurrent) null else
            twitter?.let {
                UIPreviewField(
                    title = Strings.twitter,
                    text = it,
                    icon = UIPreviewField.Icon.Twitter,
                    isLink = true
                )
            },
        if (!isCurrent) null else
            doximity?.let {
                UIPreviewField(
                    title = Strings.doximity,
                    text = it,
                    icon = UIPreviewField.Icon.Doximity,
                    isLink = true
                )
            },
    ),
    UIGroup.Preview(
        if (isCurrent && type == User.Type.Doctor) {
            UIPreviewField(
                content = UIPreviewField.Content.Button(Strings.becomeExpert, MyProfileFeature.Msg.BecomeExpert),
            )
        } else null
    ),
)