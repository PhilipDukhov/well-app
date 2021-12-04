package com.well.modules.features.myProfile.myProfileFeature

import com.well.modules.models.User
import com.well.modules.models.date.Date
import com.well.modules.models.formatters.DateFormatter
import com.well.modules.models.formatters.format
import com.well.modules.utils.kotlinUtils.ifTrueOrNull
import com.well.modules.utils.kotlinUtils.letIfTrueOrNull
import com.well.modules.utils.kotlinUtils.spacedUppercaseName

internal fun User.previewGroups(isCurrent: Boolean, hasAvailableAvailabilities: Boolean?) =
    listOfNotNull(
        if (hasAvailableAvailabilities != null) {
            UIGroup.Preview(
                UIPreviewField(
                    content = UIPreviewField.Content.Button(
                        title = Strings.requestConsultation,
                        msg = MyProfileFeature.Msg.RequestConsultation,
                        enabled = hasAvailableAvailabilities
                    ),
                )
            )
        } else null,
        UIGroup.Preview(
            credentials?.name?.letIfTrueOrNull(isCurrent) {
                UIPreviewField(title = Strings.credentials, text = it)
            },
            academicRank?.spacedUppercaseName()?.let {
                UIPreviewField(title = Strings.academicRank, text = it)
            },
            ifTrueOrNull(skills.isNotEmpty()) {
                UIPreviewField(
                    title = Strings.skillsExpertise,
                    set = skills
                        .map { it.spacedUppercaseName() }
                        .sorted()
                )
            },
        ),
        ifTrueOrNull(isCurrent) {
            UIGroup.Preview(
                email?.let {
                    UIPreviewField(title = Strings.emailAddress, text = it)
                },
                phoneNumber?.let {
                    UIPreviewField(title = Strings.phoneNumber, text = it)
                },
            )
        },
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
            ifTrueOrNull(languages.isNotEmpty()) {
                UIPreviewField(
                    title = Strings.languagesSpoken,
                    set = languages
                        .map { it.spacedUppercaseName() }
                        .sorted()
                )
            },
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
            twitter?.letIfTrueOrNull(isCurrent) {
                UIPreviewField(
                    title = Strings.twitter,
                    text = it,
                    icon = UIPreviewField.Icon.Twitter,
                    isLink = true
                )
            },
            doximity?.letIfTrueOrNull(isCurrent) {
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
                    content = UIPreviewField.Content.Button(
                        Strings.becomeExpert,
                        MyProfileFeature.Msg.BecomeExpert
                    ),
                )
            } else null
        ),
    )