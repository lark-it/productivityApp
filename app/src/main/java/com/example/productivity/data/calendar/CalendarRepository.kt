package com.example.productivity.data.calendar

import com.example.productivity.data.tasks.TaskEntity

interface CalendarRepository {
    suspend fun insertTask(task: TaskEntity)
    suspend fun getAllTasks(): List<TaskEntity>
    suspend fun deleteTask(task: TaskEntity)
    suspend fun getTasksByDate(date: String): List<TaskEntity>
    suspend fun getTasksByMonth(month: String): List<TaskEntity>
    suspend fun getCompletedCountByDate(date: String): Int
    suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int
    suspend fun getTotalTaskOccurrencesBetweenDates(startDate: String, endDate: String): Int
    suspend fun getTotalTaskOccurrencesByDate(date: String): Int
    suspend fun getTaskById(taskId: Int): TaskEntity?
    suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean)
    suspend fun getAllTasksForDate(date: String): List<TaskEntity>
    suspend fun isTaskCompleted(taskId: Int): Boolean
}
