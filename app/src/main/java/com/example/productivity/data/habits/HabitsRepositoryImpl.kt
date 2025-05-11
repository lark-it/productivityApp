package com.example.productivity.habits

import com.example.productivity.data.habits.HabitCompletionDao
import com.example.productivity.data.habits.HabitCompletionEntity
import com.example.productivity.data.habits.HabitsDao
import com.example.productivity.data.habits.HabitsEntity
import com.example.productivity.data.habits.RepeatType
import com.example.productivity.data.auth.HabitBonusEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HabitsRepositoryImpl(
    private val habitsDao: HabitsDao,
    private val completionDao: HabitCompletionDao
) : HabitsRepository {
    override suspend fun insertHabit(habit: HabitsEntity) = withContext(Dispatchers.IO) {
        habitsDao.insertHabit(habit)
    }

    override suspend fun getAllHabits(): List<HabitsEntity> = withContext(Dispatchers.IO) {
        habitsDao.getAllHabits()
    }

    override suspend fun updateHabitCompletionFlag(habitId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        habitsDao.updateHabit(habitId, isCompleted)
    }

    override suspend fun updateHabitDetails(
        id: Int,
        title: String,
        iconResId: Int,
        color: Int,
        repeatType: RepeatType,
        repeatDays: List<Int>?
    ) = withContext(Dispatchers.IO) {
        habitsDao.updateHabit(
            id,
            title,
            iconResId,
            color,
            repeatType,
            repeatDays ?: emptyList()
        )
    }

    override suspend fun deleteHabit(habit: HabitsEntity) = withContext(Dispatchers.IO) {
        habitsDao.deleteHabit(habit)
    }

    override suspend fun getTotalHabitsCompleted(): Int = withContext(Dispatchers.IO) {
        habitsDao.getTotalHabitsCompleted()
    }

    override suspend fun getCompletedHabitsOnDate(date: String): Int = withContext(Dispatchers.IO) {
        habitsDao.getCompletedHabitsOnDate(date)
    }

    override suspend fun insertCompletion(completion: HabitCompletionEntity) = withContext(Dispatchers.IO) {
        completionDao.insertCompletion(completion)
    }

    override suspend fun isHabitCompleted(habitId: Int, date: String): Boolean? = withContext(Dispatchers.IO) {
        completionDao.isHabitCompleted(habitId, date)
    }

    override suspend fun deleteCompletion(habitId: Int, date: String) = withContext(Dispatchers.IO) {
        completionDao.deleteCompletion(habitId, date)
    }

    override suspend fun getCompletedDates(habitId: Int): List<String> = withContext(Dispatchers.IO) {
        completionDao.getCompletedDates(habitId)
    }

    override suspend fun getAllCompletions(): List<HabitCompletionEntity> = withContext(Dispatchers.IO) {
        completionDao.getAllCompletedDates()
    }

    override suspend fun getTotalDaysWithCompletedHabits(): Int = withContext(Dispatchers.IO) {
        completionDao.getTotalDaysWithCompletedHabits()
    }

    override suspend fun getPerfectDays(): Int = withContext(Dispatchers.IO) {
        completionDao.getPerfectDays()
    }

    override suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        completionDao.getCompletedCountBetweenDates(startDate, endDate)
    }

    override suspend fun getTotalHabitOccurrencesBetweenDates(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        completionDao.getTotalHabitOccurrencesBetweenDates(startDate, endDate)
    }

    override suspend fun hasBonusBeenAwarded(habitId: Int, weekStart: String, weekEnd: String): Boolean = withContext(Dispatchers.IO) {
        completionDao.hasBonusBeenAwarded(habitId, weekStart, weekEnd)
    }

    override suspend fun markBonusAwarded(habitId: Int, weekStart: String, weekEnd: String) = withContext(Dispatchers.IO) {
        completionDao.markBonusAwarded(habitId, weekStart, weekEnd)
    }

    override suspend fun resetBonus(habitId: Int, weekStart: String, weekEnd: String) = withContext(Dispatchers.IO) {
        completionDao.resetBonus(habitId, weekStart, weekEnd)
    }

    override suspend fun markBonusEntity(bonus: HabitBonusEntity) = withContext(Dispatchers.IO) {
        completionDao.markBonusAwarded(bonus)
    }

    override suspend fun revokeBonus(habitId: Int, weekStart: String, weekEnd: String) = withContext(Dispatchers.IO) {
        completionDao.revokeBonus(habitId, weekStart, weekEnd)
    }

    override suspend fun getCompletedHabitEntitiesOnDate(date: String): List<HabitCompletionEntity> = withContext(Dispatchers.IO) {
        completionDao.getCompletedHabitsOnDate(date)
    }
}