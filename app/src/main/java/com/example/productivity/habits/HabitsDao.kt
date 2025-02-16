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

    @Query("UPDATE habits SET title = :newTitle, iconResId = :newIcon, color = :newColor WHERE id = :habitId")
    suspend fun updateHabit(habitId: Int, newTitle: String, newIcon: Int, newColor: Int)

    @Delete
    suspend fun deleteHabit(habit: HabitsEntity)
}
