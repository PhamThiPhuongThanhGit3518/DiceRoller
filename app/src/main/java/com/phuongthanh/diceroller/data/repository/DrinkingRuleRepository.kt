package com.phuongthanh.diceroller.data.repository

import com.phuongthanh.diceroller.data.local.room.DrinkingRuleDao
import com.phuongthanh.diceroller.data.local.room.DrinkingRuleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class DrinkingRuleRepository(private val dao: DrinkingRuleDao) {

    val allRulesFlow: Flow<List<DrinkingRuleEntity>> = dao.getAllRulesFlow()

    suspend fun getRandomRule(): DrinkingRuleEntity? = withContext(Dispatchers.IO) {
        val rules = dao.getAllRules()
        if (rules.isEmpty()) null else rules.random()
    }

    suspend fun addRule(title: String, iconEmoji: String, colorHex: String) = withContext(Dispatchers.IO) {
        val currentCount = dao.getCount()
        val rule = DrinkingRuleEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            iconEmoji = iconEmoji.ifBlank { "🍺" },
            colorHex = colorHex.ifBlank { "#EF4444" },
            isCustom = true,
            orderIndex = currentCount + 1
        )
        dao.insertRule(rule)
    }

    suspend fun updateRule(id: String, title: String, iconEmoji: String, colorHex: String, orderIndex: Int) = withContext(Dispatchers.IO) {
        val rule = DrinkingRuleEntity(
            id = id,
            title = title.trim(),
            iconEmoji = iconEmoji.ifBlank { "🍺" },
            colorHex = colorHex.ifBlank { "#EF4444" },
            isCustom = true,
            orderIndex = orderIndex
        )
        dao.updateRule(rule)
    }

    suspend fun deleteRule(id: String) = withContext(Dispatchers.IO) {
        dao.deleteRuleById(id)
    }

    suspend fun resetToDefault() = withContext(Dispatchers.IO) {
        dao.deleteAll()
        val defaultRules = listOf(
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Uống 1 ly / Drink 1 cup", "🍺", "#EF4444", false, 1),
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Uống nửa ly / Drink half cup", "🍻", "#F59E0B", false, 2),
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Chỉ định người khác / Pick someone to drink", "👉", "#3B82F6", false, 3),
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Tất cả cùng cạn ly / Cheers everyone", "🥂", "#8B5CF6", false, 4),
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Uống giao bôi / Cross-arm drink", "🤝", "#EC4899", false, 5),
            DrinkingRuleEntity(UUID.randomUUID().toString(), "Qua lượt may mắn / Lucky pass", "🍀", "#10B981", false, 6)
        )
        dao.insertAll(defaultRules)
    }

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.getCount() == 0) {
            resetToDefault()
        }
    }
}
