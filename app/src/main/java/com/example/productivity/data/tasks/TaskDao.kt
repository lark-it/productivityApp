package com.example.productivity.data.tasks

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getTasksByDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE date LIKE :month || '%'")
    suspend fun getTasksByMonth(month: String): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date AND isCompleted = 1")
    suspend fun getCompletedCountByDate(date: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE date BETWEEN :startDate AND :endDate AND isCompleted = 1")
    suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalTaskOccurrencesBetweenDates(startDate: String, endDate: String): Int

    @Query("SELECT COUNT(*) FROM tasks WHERE date = :date")
    suspend fun getTotalTaskOccurrencesByDate(date: String): Int

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): TaskEntity?

    @Query("UPDATE tasks SET isCompleted = :isCompleted WHERE id = :taskId")
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)

    @Query("SELECT * FROM tasks WHERE date = :date")
    suspend fun getAllTasksForDate(date: String): List<TaskEntity>

    @Query("SELECT isCompleted FROM tasks WHERE id = :taskId")
    suspend fun isTaskCompleted(taskId: Int): Boolean

}
