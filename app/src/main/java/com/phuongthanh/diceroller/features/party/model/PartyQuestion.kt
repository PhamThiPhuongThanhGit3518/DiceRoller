package com.phuongthanh.diceroller.features.party.model

import androidx.annotation.StringRes
import com.phuongthanh.diceroller.R

enum class QuestionType {
    TRUTH,
    DARE
}

data class PartyQuestion(
    val id: String,
    val type: QuestionType,
    @get:StringRes val textRes: Int
) {
    constructor(intId: Int, type: QuestionType, @StringRes textRes: Int) : this(intId.toString(), type, textRes)
}

object PartyQuestionsBank {
    val questions = listOf(
        // TRUTHS
        PartyQuestion(1, QuestionType.TRUTH, R.string.party_q_1_truth),
        PartyQuestion(2, QuestionType.TRUTH, R.string.party_q_2_truth),
        PartyQuestion(3, QuestionType.TRUTH, R.string.party_q_3_truth),
        PartyQuestion(4, QuestionType.TRUTH, R.string.party_q_4_truth),
        PartyQuestion(5, QuestionType.TRUTH, R.string.party_q_5_truth),
        PartyQuestion(6, QuestionType.TRUTH, R.string.party_q_6_truth),
        PartyQuestion(7, QuestionType.TRUTH, R.string.party_q_7_truth),
        PartyQuestion(8, QuestionType.TRUTH, R.string.party_q_8_truth),
        PartyQuestion(9, QuestionType.TRUTH, R.string.party_q_9_truth),
        PartyQuestion(10, QuestionType.TRUTH, R.string.party_q_10_truth),
        PartyQuestion(11, QuestionType.TRUTH, R.string.party_q_11_truth),
        PartyQuestion(12, QuestionType.TRUTH, R.string.party_q_12_truth),

        // DARES
        PartyQuestion(13, QuestionType.DARE, R.string.party_q_13_dare),
        PartyQuestion(14, QuestionType.DARE, R.string.party_q_14_dare),
        PartyQuestion(15, QuestionType.DARE, R.string.party_q_15_dare),
        PartyQuestion(16, QuestionType.DARE, R.string.party_q_16_dare),
        PartyQuestion(17, QuestionType.DARE, R.string.party_q_17_dare),
        PartyQuestion(18, QuestionType.DARE, R.string.party_q_18_dare),
        PartyQuestion(19, QuestionType.DARE, R.string.party_q_19_dare),
        PartyQuestion(20, QuestionType.DARE, R.string.party_q_20_dare),
        PartyQuestion(21, QuestionType.DARE, R.string.party_q_21_dare),
        PartyQuestion(22, QuestionType.DARE, R.string.party_q_22_dare)
    )

    fun getRandomQuestion(type: QuestionType): PartyQuestion {
        val filtered = questions.filter { it.type == type }
        return filtered.random()
    }
}
