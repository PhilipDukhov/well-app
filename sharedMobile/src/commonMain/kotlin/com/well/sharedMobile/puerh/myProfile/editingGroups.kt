package com.well.sharedMobile.puerh.myProfile

import com.well.modules.models.User
import com.well.sharedMobile.puerh.myProfile.MyProfileFeature.Msg
import com.well.sharedMobile.puerh.πModels.UIEditingField
import com.well.sharedMobile.utils.timeZonesIdentifiersList

internal fun User.editingGroups() = listOf(
    UIGroup.Editing(
        Strings.about,
        listOf(
            UIEditingField(
                placeholder = Strings.fullName,
                UIEditingField.Content.Text(textNonNullable = fullName),
            ) { Msg.UpdateUser(copy(fullName = it.text)) },
            UIEditingField.createSingleSelectionList(
                placeholder = Strings.credentials,
                singleSelection = credentials,
            ) { Msg.UpdateUser(copy(credentials = it)) },
            UIEditingField.createSingleSelectionList(
                placeholder = Strings.academicRank,
                singleSelection = academicRank,
            ) { Msg.UpdateUser(copy(academicRank = it)) },
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.skillsExpertise,
                selection = skills,
            ) { Msg.UpdateUser(copy(skills = it.toSet())) },
            UIEditingField(
                placeholder = Strings.emailAddress,
                UIEditingField.Content.Email(emailNullable = email),
            ) { Msg.UpdateUser(copy(email = it.email)) },
            UIEditingField(
                placeholder = Strings.phoneNumber,
                UIEditingField.Content.Text(textNullable = phoneNumber),
            ) { Msg.UpdateUser(copy(phoneNumber = it.text)) },
        )
    ),
    UIGroup.Editing(
        Strings.country,
        listOf(
            UIEditingField(
                placeholder = Strings.yourCountry,
                content = UIEditingField.Content.List.countryCodesList(countryCode),
                updateMsg = {
                    Msg.UpdateUser(copy(countryCode = it.selectedItems.firstOrNull()))
                }
            ),
            UIEditingField(
                placeholder = Strings.timeZone,
                content = UIEditingField.Content.List.createSingleStingSelection(
                    singleSelection = timeZoneIdentifier,
                    items = timeZonesIdentifiersList(),
                ),
                updateMsg = {
                    Msg.UpdateUser(copy(timeZoneIdentifier = it.selectedItems.firstOrNull()))
                }
            ),
        )
    ),
    UIGroup.Editing(
        Strings.more,
        listOf(
            UIEditingField.createMultipleSelectionList(
                placeholder = Strings.languagesSpoken,
                selection = languages,
            ) { Msg.UpdateUser(copy(languages = it)) },
            UIEditingField(
                placeholder = Strings.missionBio,
                UIEditingField.Content.Text(textNullable = education),
            ) { Msg.UpdateUser(copy(education = it.text)) },
            UIEditingField(
                placeholder = Strings.professionalMemberships,
                UIEditingField.Content.Text(textNullable = professionalMemberships),
            ) { Msg.UpdateUser(copy(professionalMemberships = it.text)) },
        )
    ),
    UIGroup.Editing(
        Strings.links,
        listOf(
            UIEditingField(
                placeholder = Strings.publications,
                UIEditingField.Content.Text(textNullable = publications),
            ) { Msg.UpdateUser(copy(publications = it.text)) },
            UIEditingField(
                placeholder = "${Strings.twitter} ${Strings.link}",
                UIEditingField.Content.Text(textNullable = twitter),
            ) { Msg.UpdateUser(copy(twitter = it.text)) },
            UIEditingField(
                placeholder = "${Strings.doximity} ${Strings.link}",
                UIEditingField.Content.Text(textNullable = doximity),
            ) { Msg.UpdateUser(copy(doximity = it.text)) },
        )
    ),
)