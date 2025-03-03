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

}



