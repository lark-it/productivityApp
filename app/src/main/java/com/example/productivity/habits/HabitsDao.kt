package com.example.productivity.habits

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HabitsDao {
    @Insert
    suspend fun insertHabit(habit: HabitsEntity)

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitsEntity>

    @Query("UPDATE habits SET isCompleted = :isCompleted WHERE id = :habitId")
    suspend fun updateHabit(habitId: Int, isCompleted: Boolean)

    @Query("UPDATE habits SET title = :title, iconResId = :iconResId, color = :color," +
            " repeatType = :repeatType, repeatDays = :repeatDays WHERE id = :id")
    suspend fun updateHabit(id: Int, title: String, iconResId: Int, color: Int, repeatType: RepeatType, repeatDays: List<Int>)

    @Query("SELECT COUNT(DISTINCT habitId) FROM habit_completion WHERE isCompleted = 1")
    suspend fun getTotalHabitsCompleted(): Int


    @Query("SELECT COUNT(*) FROM habit_completion WHERE date = :date")
    suspend fun getCompletedHabitsOnDate(date: String): Int

    @Delete
    suspend fun deleteHabit(habit: HabitsEntity)
}
