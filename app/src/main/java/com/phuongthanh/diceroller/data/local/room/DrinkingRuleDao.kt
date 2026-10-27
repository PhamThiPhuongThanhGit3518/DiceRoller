package com.phuongthanh.diceroller.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkingRuleDao {

    @Query("SELECT * FROM drinking_rules ORDER BY orderIndex ASC")
    fun getAllRulesFlow(): Flow<List<DrinkingRuleEntity>>

    @Query("SELECT * FROM drinking_rules ORDER BY orderIndex ASC")
    suspend fun getAllRules(): List<DrinkingRuleEntity>

    @Query("SELECT COUNT(*) FROM drinking_rules")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: DrinkingRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<DrinkingRuleEntity>)

    @Update
    suspend fun updateRule(rule: DrinkingRuleEntity)

    @Query("DELETE FROM drinking_rules WHERE id = :id")
    suspend fun deleteRuleById(id: String)

    @Query("DELETE FROM drinking_rules")
    suspend fun deleteAll()
}
