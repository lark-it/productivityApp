package com.example.productivity.habits

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HabitCompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletionEntity)

    @Query("SELECT isCompleted FROM habit_completion WHERE habitId = :habitId AND date = :date")
    suspend fun isHabitCompleted(habitId: Int, date: String): Boolean?

    @Query("DELETE FROM habit_completion WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Int, date: String)

    @Query("SELECT date FROM habit_completion WHERE habitId = :habitId")
    suspend fun getCompletedDates(habitId: Int): List<String>

    @Query("SELECT habitId, date, isCompleted FROM habit_completion")
    suspend fun getAllCompletedDates(): List<HabitCompletionEntity>

    @Query("SELECT COUNT(DISTINCT date) FROM habit_completion")
    suspend fun getTotalDaysWithCompletedHabits(): Int

    @Query("SELECT COUNT(DISTINCT date) FROM habit_completion WHERE isCompleted = 1")
    suspend fun getPerfectDays(): Int

    @Query("SELECT COUNT(*) FROM habit_completion WHERE date = :date AND isCompleted = 1")
    suspend fun getCompletedCountByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM habit_completion WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 1")
    suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM habit_completion WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalHabitOccurrencesBetweenDates(startDate: String, endDate: String): Int



}
