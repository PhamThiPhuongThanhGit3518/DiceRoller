package com.phuongthanh.diceroller.data.repository

import android.content.Context
import com.phuongthanh.diceroller.data.local.room.PartyQuestionDao
import com.phuongthanh.diceroller.data.local.room.PartyQuestionEntity
import com.phuongthanh.diceroller.features.party.model.PartyQuestionsBank
import com.phuongthanh.diceroller.features.party.model.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class PartyQuestionRepository(
    private val dao: PartyQuestionDao,
    private val context: Context
) {

    val allQuestionsFlow: Flow<List<PartyQuestionEntity>> = dao.getAllQuestionsFlow()

    fun getQuestionsByTypeFlow(type: QuestionType): Flow<List<PartyQuestionEntity>> {
        return dao.getQuestionsByTypeFlow(if (type == QuestionType.TRUTH) "TRUTH" else "DARE")
    }

    suspend fun getRandomQuestion(type: QuestionType): PartyQuestionEntity? = withContext(Dispatchers.IO) {
        val typeStr = if (type == QuestionType.TRUTH) "TRUTH" else "DARE"
        val list = dao.getQuestionsByType(typeStr)
        if (list.isEmpty()) {
            // Fallback or reseed
            null
        } else {
            list.random()
        }
    }

    suspend fun addQuestion(type: QuestionType, text: String) = withContext(Dispatchers.IO) {
        val entity = PartyQuestionEntity(
            id = UUID.randomUUID().toString(),
            type = if (type == QuestionType.TRUTH) "TRUTH" else "DARE",
            text = text.trim(),
            isCustom = true
        )
        dao.insertQuestion(entity)
    }

    suspend fun updateQuestion(id: String, type: QuestionType, text: String) = withContext(Dispatchers.IO) {
        val entity = PartyQuestionEntity(
            id = id,
            type = if (type == QuestionType.TRUTH) "TRUTH" else "DARE",
            text = text.trim(),
            isCustom = true
        )
        dao.updateQuestion(entity)
    }

    suspend fun deleteQuestion(id: String) = withContext(Dispatchers.IO) {
        dao.deleteQuestionById(id)
    }

    suspend fun resetToDefault() = withContext(Dispatchers.IO) {
        dao.deleteAll()
        val defaultEntities = PartyQuestionsBank.questions.map { q ->
            PartyQuestionEntity(
                id = UUID.randomUUID().toString(),
                type = if (q.type == QuestionType.TRUTH) "TRUTH" else "DARE",
                text = context.getString(q.textRes),
                isCustom = false
            )
        }
        dao.insertAll(defaultEntities)
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            resetToDefault()
        }
    }
}
