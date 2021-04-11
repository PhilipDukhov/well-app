package com.well.modules.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: UserId,
    val initialized: Boolean,
    val fullName: String,
    val type: Type,
    val email: String? = null,
    val profileImageUrl: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null,
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
) {
    val completeness: Int
        get() = listOf(
            id,
            fullName,
            email,
            profileImageUrl,
            phoneNumber,
            location,
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
        ).run {
            100 * count { it != null && (it as? Collection<*>)?.isEmpty() != true } / count()
        }

    @Serializable
    enum class Credentials {
        MD,
        DO,
    }

    @Serializable
    enum class Type {
        Doctor,
        Expert,
    }

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

    @Suppress("SpellCheckingInspection")
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
}