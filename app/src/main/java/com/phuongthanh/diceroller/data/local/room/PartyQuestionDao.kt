package com.phuongthanh.diceroller.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PartyQuestionDao {

    @Query("SELECT * FROM party_questions ORDER BY createdAt ASC")
    fun getAllQuestionsFlow(): Flow<List<PartyQuestionEntity>>

    @Query("SELECT * FROM party_questions WHERE type = :type ORDER BY createdAt ASC")
    fun getQuestionsByTypeFlow(type: String): Flow<List<PartyQuestionEntity>>

    @Query("SELECT * FROM party_questions WHERE type = :type")
    suspend fun getQuestionsByType(type: String): List<PartyQuestionEntity>

    @Query("SELECT COUNT(*) FROM party_questions")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: PartyQuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<PartyQuestionEntity>)

    @Update
    suspend fun updateQuestion(question: PartyQuestionEntity)

    @Query("DELETE FROM party_questions WHERE id = :id")
    suspend fun deleteQuestionById(id: String)

    @Query("DELETE FROM party_questions")
    suspend fun deleteAll()
}
