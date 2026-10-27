package com.phuongthanh.diceroller.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.phuongthanh.diceroller.features.party.model.PartyQuestionsBank
import com.phuongthanh.diceroller.features.party.model.QuestionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        PartyQuestionEntity::class,
        DrinkingRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun partyQuestionDao(): PartyQuestionDao
    abstract fun drinkingRuleDao(): DrinkingRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diceroller_database.db"
                ).addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(context, database)
                    }
                }
            }
        }

        suspend fun populateInitialData(context: Context, database: AppDatabase) {
            // Seed default party questions if empty
            if (database.partyQuestionDao().getCount() == 0) {
                val questions = PartyQuestionsBank.questions.map { q ->
                    PartyQuestionEntity(
                        id = UUID.randomUUID().toString(),
                        type = if (q.type == QuestionType.TRUTH) "TRUTH" else "DARE",
                        text = context.getString(q.textRes),
                        isCustom = false
                    )
                }
                database.partyQuestionDao().insertAll(questions)
            }

            // Seed default drinking rules if empty
            if (database.drinkingRuleDao().getCount() == 0) {
                val defaultRules = listOf(
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_1), "🍺", "#EF4444", false, 1),
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_2), "🍻", "#F59E0B", false, 2),
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_3), "👉", "#3B82F6", false, 3),
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_4), "🥂", "#8B5CF6", false, 4),
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_5), "🤝", "#EC4899", false, 5),
                    DrinkingRuleEntity(UUID.randomUUID().toString(), context.getString(com.phuongthanh.diceroller.R.string.drinking_rule_6), "🍀", "#10B981", false, 6)
                )
                database.drinkingRuleDao().insertAll(defaultRules)
            }
        }
    }
}
