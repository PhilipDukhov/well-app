package com.well.sharedMobileTest

import com.well.modules.atomic.AtomicMutableList
import com.well.modules.models.Availability
import com.well.modules.models.Meeting
import com.well.modules.models.MeetingViewModel
import com.well.modules.models.Repeat
import com.well.modules.models.User
import com.well.modules.models.chat.ChatMessage
import com.well.modules.models.chat.ChatMessageViewModel
import com.well.modules.models.date.Date
import com.well.modules.models.date.dateTime.daysShift
import com.well.modules.models.date.dateTime.time
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random
import kotlin.time.Duration

val User.Companion.testUser
    get() = User(
        id = User.Id(1),
        initialized = true,
        lastEdited = 0.0,
        fullName = "Phil Dukhov",
//        profileImageUrl = "https://i.imgur.com/StXm8nf.jpg",
        profileImageUrl = "https://s3.us-east-2.amazonaws.com/well-images/profilePictures/3-932c9f30-d066-43ac-beef-a558ea9d07fa..jpeg",
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
        favorite = false,
        email = "phil@gmail.com",
        countryCode = "UA",
    )

private val testMessages = AtomicMutableList<ChatMessage>()
private val loremImpsum =
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."

fun ChatMessage.Companion.getTestMessages(count: Int): List<ChatMessage> {
    while (testMessages.count() < count) {
        testMessages.add(
            ChatMessage(
                id = ChatMessage.Id(testMessages.count().toLong()),
                creation = Date().millis.toDouble() / 1000 + Random.nextDouble(-10000.0, 10000.0),
                fromId = User.Id(if (testMessages.count() % 2 == 0) 0 else 1),
                peerId = User.Id(if (testMessages.count() % 2 == 0) 1 else 0),
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

fun ChatMessageViewModel.Companion.getTestMessagesWithStatus(count: Int): List<ChatMessageViewModel> =
    ChatMessage.getTestMessages(count).mapIndexed { i, message ->
        ChatMessageViewModel(
            id = message.id,
            status = ChatMessageViewModel.Status.values()
                .let { values ->
                    values[i % values.count()]
                }, 
            creation = message.creation,
            content = message.content,
        )
    }


private data class TestAvailabilitiesState(
    val daysOffset: Int,
    val repeat: Repeat,
    val startTime: Int,
    val hoursDuration: Int,
) {
    fun availability(i: Int) =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let { now ->
            Availability(
                id = Availability.Id(i.toLong()),
                startDay = now.date.daysShift(i),
                startTime = now.time,
                durationMinutes = Duration.hours(hoursDuration).inWholeMinutes.toInt(),
                repeat = repeat,
            )
        }
}

fun Availability.Companion.testValues(count: Int) = List(count) { i ->
    TestAvailabilitiesState(
        daysOffset = i + Random.nextInt(4),
        repeat = Repeat.values().random(),
        startTime = Random.nextInt(8, 16),
        hoursDuration = Random.nextInt(1, 5),
    )
}.mapIndexed { i, state ->
    state.availability(i)
}

fun MeetingViewModel.Companion.testValues(count: Int) = List(count) { i ->
    if (i < 4) {
        MeetingViewModel(
            id = Meeting.Id(value = i.toLong()),
            startInstant = Clock.System.now() + Duration.minutes((i - 1) * 20),
            durationMinutes = 15,
            user = User.testUser
        )
    } else {
        MeetingViewModel(
            id = Meeting.Id(value = i.toLong()),
            startInstant = Clock.System.now() + Duration.days(i) + Duration.hours(Random.nextInt(-10,
                10)),
            durationMinutes = Random.nextInt(10, 90),
            user = User.testUser
        )
    }
}