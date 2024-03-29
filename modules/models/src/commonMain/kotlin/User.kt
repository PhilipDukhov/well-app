package com.well.modules.models

import com.well.modules.models.formatters.initialsFromName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
data class User(
    val id: Id,
    val initialized: Boolean = true,
    val lastEdited: Double,
    val favorite: Boolean = false,
    val fullName: String,
    val type: Type,
    val email: String? = null,
    val reviewInfo: ReviewInfo,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val countryCode: String? = null,
    val timeZoneIdentifier: String? = null,
    val credentials: Credentials? = null,
    val academicRank: AcademicRank? = null,
    val languages: Set<Language> = setOf(),
    val skills: Set<Skill> = setOf(),
    val bio: String? = null,
    val education: String? = null,
    val professionalMemberships: String? = null,
    val publications: String? = null,
    val twitter: String? = null,
    val doximity: String? = null,
    val isOnline: Boolean = false,
) {
    @Serializable
    @JvmInline
    value class Id(val value: Long) {
        override fun toString() = value.toString()

        object ColumnAdapter: com.squareup.sqldelight.ColumnAdapter<Id, Long> {
            override fun decode(databaseValue: Long): Id =
                Id(databaseValue)

            override fun encode(value: Id): Long =
                value.value
        }
    }

    val completeness: Int
        get() = listOf(
            id,
            fullName,
            email,
            profileImageUrl,
            phoneNumber,
            countryCode,
            timeZoneIdentifier,
            credentials,
            academicRank,
            languages,
            skills,
            bio,
            education,
            professionalMemberships,
            publications,
            twitter,
            doximity,
        ).let { list ->
            100 * list.count { it != null && (it as? Collection<*>)?.isEmpty() != true } / list.count()
        }

    @Suppress("unused")
    @Serializable
    enum class Credentials {
        MD,
        DO,
    }

    @Serializable
    enum class Type {
        Doctor,
        PendingExpert,
        DeclinedExpert,
        Expert,
    }

    @Suppress("unused")
    @Serializable
    enum class AcademicRank {
        Professor,
        AssociateProfessor,
        AssistantProfessor,
        ClinicalInstructor,
        Fellow,
        Resident,
        ;
    }

    @Suppress("SpellCheckingInspection", "unused")
    @Serializable
    enum class Skill {
        Endoscopy,
        Laparoscopy,
        RoboticSurgery,
        Ureteroscopy,
        ShockWaveLithotripsy,
        ShockWaveTherapy,
        PercutaneousNephrolithotomy,
        LaparoscopicRenalSurgery,
        RoboticRenalSurgery,
        RoboticProstatectomy,
        RoboticCystectomy,
        RoboticUrinaryReconstructionSurgery,
        UrolithiasisPrevention,
        KidneyCancer,
        ProstateCancer,
        BladderCancer,
        UpperTractUrothelialCarcinoma,
        Lasers,
        BPH,
    }

    @Suppress("unused")
    @Serializable
    enum class Language {
        English,
        Spanish,
        Portuguese,
        Chinese,
        Russian,
        Arabic,
        French,
    }

    @Serializable
    data class ReviewInfo(
        val count: Int,
        val average: Double,
        val currentUserReview: Review? = null,
    )

    val initials = fullName.initialsFromName()
}