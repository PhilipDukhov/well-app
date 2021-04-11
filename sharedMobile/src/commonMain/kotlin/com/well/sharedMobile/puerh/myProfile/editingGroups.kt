package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.User
import com.well.modules.models.formatters.timeZonesIdentifiersList

internal fun User.editingGroups() = listOf(
    UIGroup.Editing(
        Strings.about,
        listOf(
            UIEditingField(
                placeholder = Strings.fullName,
                UIEditingField.Content.Text(textNonNullable = fullName),
            ) { MyProfileFeature.Msg.UpdateUser(copy(fullName = it.text)) },
            UIEditingField.createSingleSelectionList(
                placeholder = Strings.credentials,
                singleSelection = credentials,
            ) { MyProfileFeature.Msg.UpdateUser(copy(credentials = it)) },
            UIEditingField.createSingleSelectionList(
                placeholder = Strings.academicRank,
                singleSelection = academicRank,
            ) { MyProfileFeature.Msg.UpdateUser(copy(academicRank = it)) },
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.skillsExpertise,
                selection = skills,
            ) { MyProfileFeature.Msg.UpdateUser(copy(skills = it.toSet())) },
            UIEditingField(
                placeholder = Strings.emailAddress,
                UIEditingField.Content.Email(emailNullable = email),
            ) { MyProfileFeature.Msg.UpdateUser(copy(email = it.email)) },
            UIEditingField(
                placeholder = Strings.phoneNumber,
                UIEditingField.Content.Text(textNullable = phoneNumber),
            ) { MyProfileFeature.Msg.UpdateUser(copy(phoneNumber = it.text)) },
        )
    ),
    UIGroup.Editing(
        Strings.location,
        listOf(
            UIEditingField(
                placeholder = Strings.yourLocation,
                UIEditingField.Content.Text(
                    textNullable = location,
                    icon = UIEditingField.Content.Icon.Location
                ),
            ) { MyProfileFeature.Msg.UpdateUser(copy(location = it.text)) },
            UIEditingField(
                placeholder = Strings.timeZone,
                UIEditingField.Content.List(
                    singleSelection = timeZoneIdentifier,
                    list = timeZonesIdentifiersList(),
                ),
            ) { MyProfileFeature.Msg.UpdateUser(copy(timeZoneIdentifier = it.selection.first())) },
        )
    ),
    UIGroup.Editing(
        Strings.more,
        listOf(
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.languagesSpoken,
                selection = languages,
            ) { MyProfileFeature.Msg.UpdateUser(copy(languages = it.toSet())) },
            UIEditingField(
                placeholder = Strings.missionBio,
                UIEditingField.Content.Text(textNullable = education),
            ) { MyProfileFeature.Msg.UpdateUser(copy(education = it.text)) },
            UIEditingField(
                placeholder = Strings.professionalMemberships,
                UIEditingField.Content.Text(textNullable = professionalMemberships),
            ) { MyProfileFeature.Msg.UpdateUser(copy(professionalMemberships = it.text)) },
        )
    ),
    UIGroup.Editing(
        Strings.links,
        listOf(
            UIEditingField(
                placeholder = Strings.publications,
                UIEditingField.Content.Text(textNullable = publications),
            ) { MyProfileFeature.Msg.UpdateUser(copy(publications = it.text)) },
            UIEditingField(
                placeholder = "${Strings.twitter} ${Strings.link}",
                UIEditingField.Content.Text(textNullable = twitter),
            ) { MyProfileFeature.Msg.UpdateUser(copy(twitter = it.text)) },
            UIEditingField(
                placeholder = "${Strings.doximity} ${Strings.link}",
                UIEditingField.Content.Text(textNullable = doximity),
            ) { MyProfileFeature.Msg.UpdateUser(copy(doximity = it.text)) },
        )
    ),
)