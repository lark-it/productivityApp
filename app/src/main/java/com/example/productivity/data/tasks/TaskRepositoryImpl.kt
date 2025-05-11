package com.example.productivity.data.tasks

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskRepositoryImpl(
    private val dao: TaskDao
) : TaskRepository {

    override suspend fun insertTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.insertTask(task)
    }

    override suspend fun getAllTasks(): List<TaskEntity> = withContext(Dispatchers.IO) {
        dao.getAllTasks()
    }

    override suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        dao.deleteTask(task)
    }

    override suspend fun getTasksByDate(date: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        dao.getTasksByDate(date)
    }

    override suspend fun getTasksByMonth(month: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        dao.getTasksByMonth(month)
    }

    override suspend fun getCompletedCountByDate(date: String): Int = withContext(Dispatchers.IO) {
        dao.getCompletedCountByDate(date)
    }

    override suspend fun getCompletedCountBetweenDates(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        dao.getCompletedCountBetweenDates(startDate, endDate)
    }

    override suspend fun getTotalTaskOccurrencesBetweenDates(startDate: String, endDate: String): Int = withContext(Dispatchers.IO) {
        dao.getTotalTaskOccurrencesBetweenDates(startDate, endDate)
    }

    override suspend fun getTotalTaskOccurrencesByDate(date: String): Int = withContext(Dispatchers.IO) {
        dao.getTotalTaskOccurrencesByDate(date)
    }

    override suspend fun getTaskById(taskId: Int): TaskEntity? = withContext(Dispatchers.IO) {
        dao.getTaskById(taskId)
    }

    override suspend fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        dao.updateTaskCompletion(taskId, isCompleted)
    }

    override suspend fun getAllTasksForDate(date: String): List<TaskEntity> = withContext(Dispatchers.IO) {
        dao.getAllTasksForDate(date)
    }

    override suspend fun isTaskCompleted(taskId: Int): Boolean = withContext(Dispatchers.IO) {
        dao.isTaskCompleted(taskId)
    }
}
