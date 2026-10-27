package com.phuongthanh.diceroller.features.party.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

enum class ChallengeType(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int,
    @get:DrawableRes val iconRes: Int,
    val colorHex: String
) {
    FUNNY_QUESTIONS(
        R.string.type_funny_questions,
        R.string.type_funny_desc,
        R.drawable.ic_type_funny,
        "#10B981"
    ),
    QUICK_DARES(
        R.string.type_quick_dares,
        R.string.type_quick_desc,
        R.drawable.ic_type_dare,
        "#F59E0B"
    ),
    GROUP_ACTIONS(
        R.string.type_group_actions,
        R.string.type_group_desc,
        R.drawable.ic_type_group,
        "#3B82F6"
    ),
    RANDOM_TASKS(
        R.string.type_random_tasks,
        R.string.type_random_desc,
        R.drawable.ic_type_random,
        "#8B5CF6"
    )
}

data class PartyChallenge(
    val id: String,
    val type: ChallengeType,
    @get:StringRes val textRes: Int
)

data class PartyChallengePack(
    val id: String,
    @get:StringRes val nameRes: Int,
    @get:StringRes val descRes: Int,
    val challengesCount: Int,
    val playersRange: String,
    val difficulty: String,
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList(),
    @get:StringRes val tagResList: List<Int> = emptyList(),
    @get:DrawableRes val iconRes: Int = R.drawable.ic_dice_d6,
    val challenges: List<PartyChallenge>
)

object PartyPacksBank {

    private val classicPartyChallenges = listOf(
        // Funny Questions
        PartyChallenge("cp_1", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_1),
        PartyChallenge("cp_2", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_2),
        PartyChallenge("cp_3", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_3),
        PartyChallenge("cp_4", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_4),
        PartyChallenge("cp_5", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_5),
        PartyChallenge("cp_6", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_6),
        PartyChallenge("cp_7", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_7),
        PartyChallenge("cp_8", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cp_8),

        // Quick Dares
        PartyChallenge("cp_9", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_9),
        PartyChallenge("cp_10", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_10),
        PartyChallenge("cp_11", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_11),
        PartyChallenge("cp_12", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_12),
        PartyChallenge("cp_13", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_13),
        PartyChallenge("cp_14", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_14),
        PartyChallenge("cp_15", ChallengeType.QUICK_DARES, R.string.party_challenge_cp_15),

        // Group Actions
        PartyChallenge("cp_16", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_16),
        PartyChallenge("cp_17", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_17),
        PartyChallenge("cp_18", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_18),
        PartyChallenge("cp_19", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_19),
        PartyChallenge("cp_20", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_20),
        PartyChallenge("cp_21", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_21),
        PartyChallenge("cp_22", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cp_22),

        // Random Tasks
        PartyChallenge("cp_23", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_23),
        PartyChallenge("cp_24", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_24),
        PartyChallenge("cp_25", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_25),
        PartyChallenge("cp_26", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_26),
        PartyChallenge("cp_27", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_27),
        PartyChallenge("cp_28", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_28),
        PartyChallenge("cp_29", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_29),
        PartyChallenge("cp_30", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cp_30)
    )

    private val drinkingNightChallenges = listOf(
        // Funny Questions
        PartyChallenge("dn_1", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_1),
        PartyChallenge("dn_2", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_2),
        PartyChallenge("dn_3", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_3),
        PartyChallenge("dn_4", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_4),
        PartyChallenge("dn_5", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_5),
        PartyChallenge("dn_6", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_6),
        PartyChallenge("dn_7", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_dn_7),

        // Quick Dares
        PartyChallenge("dn_8", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_8),
        PartyChallenge("dn_9", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_9),
        PartyChallenge("dn_10", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_10),
        PartyChallenge("dn_11", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_11),
        PartyChallenge("dn_12", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_12),
        PartyChallenge("dn_13", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_13),
        PartyChallenge("dn_14", ChallengeType.QUICK_DARES, R.string.party_challenge_dn_14),

        // Group Actions
        PartyChallenge("dn_15", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_15),
        PartyChallenge("dn_16", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_16),
        PartyChallenge("dn_17", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_17),
        PartyChallenge("dn_18", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_18),
        PartyChallenge("dn_19", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_19),
        PartyChallenge("dn_20", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_20),
        PartyChallenge("dn_21", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_21),
        PartyChallenge("dn_22", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_dn_22),

        // Random Tasks
        PartyChallenge("dn_23", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_23),
        PartyChallenge("dn_24", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_24),
        PartyChallenge("dn_25", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_25),
        PartyChallenge("dn_26", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_26),
        PartyChallenge("dn_27", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_27),
        PartyChallenge("dn_28", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_28),
        PartyChallenge("dn_29", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_29),
        PartyChallenge("dn_30", ChallengeType.RANDOM_TASKS, R.string.party_challenge_dn_30)
    )

    private val coupleFunChallenges = listOf(
        // Funny Questions
        PartyChallenge("cf_1", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_1),
        PartyChallenge("cf_2", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_2),
        PartyChallenge("cf_3", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_3),
        PartyChallenge("cf_4", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_4),
        PartyChallenge("cf_5", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_5),
        PartyChallenge("cf_6", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cf_6),

        // Quick Dares
        PartyChallenge("cf_7", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_7),
        PartyChallenge("cf_8", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_8),
        PartyChallenge("cf_9", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_9),
        PartyChallenge("cf_10", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_10),
        PartyChallenge("cf_11", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_11),
        PartyChallenge("cf_12", ChallengeType.QUICK_DARES, R.string.party_challenge_cf_12),

        // Group Actions
        PartyChallenge("cf_13", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_13),
        PartyChallenge("cf_14", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_14),
        PartyChallenge("cf_15", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_15),
        PartyChallenge("cf_16", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_16),
        PartyChallenge("cf_17", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_17),
        PartyChallenge("cf_18", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cf_18),

        // Random Tasks
        PartyChallenge("cf_19", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_19),
        PartyChallenge("cf_20", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_20),
        PartyChallenge("cf_21", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_21),
        PartyChallenge("cf_22", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_22),
        PartyChallenge("cf_23", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_23),
        PartyChallenge("cf_24", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cf_24)
    )

    private val crazyDaresChallenges = listOf(
        // Funny Questions
        PartyChallenge("cd_1", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_1),
        PartyChallenge("cd_2", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_2),
        PartyChallenge("cd_3", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_3),
        PartyChallenge("cd_4", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_4),
        PartyChallenge("cd_5", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_5),
        PartyChallenge("cd_6", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_cd_6),

        // Quick Dares
        PartyChallenge("cd_7", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_7),
        PartyChallenge("cd_8", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_8),
        PartyChallenge("cd_9", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_9),
        PartyChallenge("cd_10", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_10),
        PartyChallenge("cd_11", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_11),
        PartyChallenge("cd_12", ChallengeType.QUICK_DARES, R.string.party_challenge_cd_12),

        // Group Actions
        PartyChallenge("cd_13", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_13),
        PartyChallenge("cd_14", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_14),
        PartyChallenge("cd_15", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_15),
        PartyChallenge("cd_16", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_16),
        PartyChallenge("cd_17", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_17),
        PartyChallenge("cd_18", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_cd_18),

        // Random Tasks
        PartyChallenge("cd_19", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_19),
        PartyChallenge("cd_20", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_20),
        PartyChallenge("cd_21", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_21),
        PartyChallenge("cd_22", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_22),
        PartyChallenge("cd_23", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_23),
        PartyChallenge("cd_24", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_24),
        PartyChallenge("cd_25", ChallengeType.RANDOM_TASKS, R.string.party_challenge_cd_25)
    )

    private val icebreakerChallenges = listOf(
        // Funny Questions
        PartyChallenge("ib_1", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_ib_1),
        PartyChallenge("ib_2", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_ib_2),
        PartyChallenge("ib_3", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_ib_3),
        PartyChallenge("ib_4", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_ib_4),
        PartyChallenge("ib_5", ChallengeType.FUNNY_QUESTIONS, R.string.party_challenge_ib_5),

        // Quick Dares
        PartyChallenge("ib_6", ChallengeType.QUICK_DARES, R.string.party_challenge_ib_6),
        PartyChallenge("ib_7", ChallengeType.QUICK_DARES, R.string.party_challenge_ib_7),
        PartyChallenge("ib_8", ChallengeType.QUICK_DARES, R.string.party_challenge_ib_8),
        PartyChallenge("ib_9", ChallengeType.QUICK_DARES, R.string.party_challenge_ib_9),
        PartyChallenge("ib_10", ChallengeType.QUICK_DARES, R.string.party_challenge_ib_10),

        // Group Actions
        PartyChallenge("ib_11", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_ib_11),
        PartyChallenge("ib_12", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_ib_12),
        PartyChallenge("ib_13", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_ib_13),
        PartyChallenge("ib_14", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_ib_14),
        PartyChallenge("ib_15", ChallengeType.GROUP_ACTIONS, R.string.party_challenge_ib_15),

        // Random Tasks
        PartyChallenge("ib_16", ChallengeType.RANDOM_TASKS, R.string.party_challenge_ib_16),
        PartyChallenge("ib_17", ChallengeType.RANDOM_TASKS, R.string.party_challenge_ib_17),
        PartyChallenge("ib_18", ChallengeType.RANDOM_TASKS, R.string.party_challenge_ib_18),
        PartyChallenge("ib_19", ChallengeType.RANDOM_TASKS, R.string.party_challenge_ib_19),
        PartyChallenge("ib_20", ChallengeType.RANDOM_TASKS, R.string.party_challenge_ib_20)
    )

    private val packs = listOf(
        PartyChallengePack(
            id = "classic_party",
            nameRes = R.string.pack_classic_party_name,
            descRes = R.string.pack_classic_party_desc,
            challengesCount = 30,
            playersRange = "2–8",
            difficulty = "Easy",
            isFeatured = true,
            tags = listOf("Party", "2–8", "Easy"),
            tagResList = listOf(R.string.tag_party, R.string.difficulty_easy),
            iconRes = R.drawable.ic_dice_d6,
            challenges = classicPartyChallenges
        ),
        PartyChallengePack(
            id = "drinking_night",
            nameRes = R.string.pack_drinking_night_name,
            descRes = R.string.pack_drinking_night_desc,
            challengesCount = 30,
            playersRange = "2–4",
            difficulty = "Easy",
            isFeatured = false,
            tags = listOf("Party", "2–4", "Easy"),
            tagResList = listOf(R.string.tag_party, R.string.difficulty_easy),
            iconRes = R.drawable.ic_trophy,
            challenges = drinkingNightChallenges
        ),
        PartyChallengePack(
            id = "couple_fun",
            nameRes = R.string.pack_couple_night_name,
            descRes = R.string.pack_couple_night_desc,
            challengesCount = 24,
            playersRange = "2",
            difficulty = "Medium",
            isFeatured = false,
            tags = listOf("Couple", "2", "Medium"),
            tagResList = listOf(R.string.tag_couple, R.string.difficulty_medium),
            iconRes = R.drawable.ic_star_rate,
            challenges = coupleFunChallenges
        ),
        PartyChallengePack(
            id = "crazy_dares",
            nameRes = R.string.pack_crazy_dares_name,
            descRes = R.string.pack_crazy_dares_desc,
            challengesCount = 25,
            playersRange = "3–6",
            difficulty = "Hard",
            isFeatured = false,
            tags = listOf("Dares", "3–6", "Hard"),
            tagResList = listOf(R.string.tag_dares, R.string.difficulty_hard),
            iconRes = R.drawable.ic_type_dare,
            challenges = crazyDaresChallenges
        ),
        PartyChallengePack(
            id = "icebreaker",
            nameRes = R.string.pack_icebreaker_name,
            descRes = R.string.pack_icebreaker_desc,
            challengesCount = 20,
            playersRange = "2–10",
            difficulty = "Easy",
            isFeatured = false,
            tags = listOf("Icebreaker", "2–10", "Easy"),
            tagResList = listOf(R.string.tag_icebreaker, R.string.difficulty_easy),
            iconRes = R.drawable.ic_type_funny,
            challenges = icebreakerChallenges
        )
    )

    fun getAllPacks(): List<PartyChallengePack> = packs

    fun getFeaturedPack(): PartyChallengePack = packs.first { it.isFeatured }

    fun getRecentPacks(): List<PartyChallengePack> = packs

    fun getRecommendedPacks(): List<PartyChallengePack> = packs.filter { !it.isFeatured }

    fun getPackById(id: String): PartyChallengePack {
        return packs.find { it.id == id } ?: getFeaturedPack()
    }

    fun getRandomChallenge(packId: String): PartyChallenge {
        val pack = getPackById(packId)
        return pack.challenges.random()
    }
}
