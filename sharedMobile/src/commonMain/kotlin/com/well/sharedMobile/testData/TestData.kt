package com.well.sharedMobile.testData

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.models.Date
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.sharedMobile.puerh.πModels.chatMessageWithStatus.ChatMessageWithStatus
import kotlin.random.Random

val User.Companion.testUser
    get() = User(
        id = 1,
        initialized = true,
        lastEdited = 0.0,
        fullName = "12",
        profileImageUrl = "https://i.imgur.com/StXm8nf.jpg",
        type = User.Type.Doctor,
        phoneNumber = "+380686042511",
        timeZoneIdentifier = "America/Los_Angeles",
        credentials = User.Credentials.MD,
        academicRank = User.AcademicRank.AssistantProfessor,
        languages = setOf(User.Language.English, User.Language.Russian),
        bio = "LA, CaliforniaLA, CaliforniaLA, CaliforniaLA, CaliforniaLA, California",
        education = "LA, CaliforniaLA, California",
        professionalMemberships = "CaliforniaLA, California",
        publications = "CaliforniaLA, California",
        twitter = "CaliforniaLA, California",
        doximity = "CaliforniaLA, California",
        skills = setOf(
            User.Skill.BPH,
            User.Skill.RoboticCystectomy,
            User.Skill.RoboticUrinaryReconstructionSurgery,
            User.Skill.RoboticRenalSurgery,
            User.Skill.PercutaneousNephrolithotomy
        ),
        ratingInfo = User.RatingInfo(
            count = 0,
            average = 0.0,
        ),
    )

private val testMessages = AtomicMutableList<ChatMessage>()
private val loremImpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

fun ChatMessage.Companion.getTestMessages(count: Int): List<ChatMessage> {
    while (testMessages.count() < count) {
        testMessages.add(
            ChatMessage(
                id = testMessages.count(),
                creation = Date().millis.toDouble() / 1000 + Random.nextDouble(-10000.0, 10000.0),
                fromId = if (testMessages.count() % 2 == 0) 0 else 1,
                peerId = if (testMessages.count() % 2 == 0) 1 else 0,
                content = ChatMessage.Content.Text(
                    Random.nextInt(loremImpsum.count())
                        .let {
                            loremImpsum.subSequence(startIndex = 0, endIndex = it).toString()
                        }
                ),
            )
        )
    }
    return testMessages.subList(0, count)
}

fun ChatMessageWithStatus.Companion.getTestMessagesWithStatus(count: Int): List<ChatMessageWithStatus> =
    ChatMessage.getTestMessages(count).map { message ->
        ChatMessageWithStatus(
            message,
            status = ChatMessageWithStatus.Status.values()
                .let { values ->
                    values[message.id % values.count()]
                }
        )
    }