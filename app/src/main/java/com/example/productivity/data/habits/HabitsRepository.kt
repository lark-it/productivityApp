package com.example.productivity.habits

import com.example.productivity.data.habits.HabitCompletionEntity
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.data.habits.RepeatType
import com.example.productivity.data.auth.HabitBonusEntity

/**
 * Репозиторий для работы с привычками и их выполнениями.
 * Оборачивает HabitsDao и HabitCompletionDao.
 */
interface HabitsRepository {
    suspend fun insertHabit(habit: HabitsEntity)
    suspend fun getAllHabits(): List<HabitsEntity>
    suspend fun updateHabitCompletionFlag(habitId: Int, isCompleted: Boolean)
    suspend fun updateHabitDetails(
        id: Int,
        title: String,
        iconResId: Int,
        color: Int,
        repeatType: RepeatType,
        repeatDays: List<Int>?
    )
    suspend fun deleteHabit(habit: HabitsEntity)
    suspend fun getTotalHabitsCompleted(): Int
    suspend fun getCompletedHabitsOnDate(date: String): Int

    suspend fun insertCompletion(completion: HabitCompletionEntity)
    suspend fun isHabitCompleted(habitId: Int, date: String): Boolean?
    suspend fun deleteCompletion(habitId: Int, date: String)
    suspend fun getCompletedDates(habitId: Int): List<String>
    suspend fun getAllCompletions(): List<HabitCompletionEntity>
    suspend fun getTotalDaysWithCompletedHabits(): Int
    suspend fun getPerfectDays(): Int
    suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int
    suspend fun getTotalHabitOccurrencesBetweenDates(startDate: String, endDate: String): Int
    suspend fun hasBonusBeenAwarded(habitId: Int, weekStart: String, weekEnd: String): Boolean
    suspend fun markBonusAwarded(habitId: Int, weekStart: String, weekEnd: String)
    suspend fun resetBonus(habitId: Int, weekStart: String, weekEnd: String)
    suspend fun markBonusEntity(bonus: HabitBonusEntity)
    suspend fun revokeBonus(habitId: Int, weekStart: String, weekEnd: String)
    suspend fun getCompletedHabitEntitiesOnDate(date: String): List<HabitCompletionEntity>
}