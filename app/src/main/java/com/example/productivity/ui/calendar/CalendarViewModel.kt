package com.example.productivity.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.productivity.data.calendar.CalendarRepository
import com.example.productivity.data.tasks.TaskEntity

/**
 * ViewModel для фичи календаря.
 * Хранит потоки данных и оборачивает все операции репозитория.
 */
class CalendarViewModel(
    private val repo: CalendarRepository
) : ViewModel() {

    private val _allTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val allTasks: StateFlow<List<TaskEntity>> = _allTasks.asStateFlow()

    private val _tasksByDate = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasksByDate: StateFlow<List<TaskEntity>> = _tasksByDate.asStateFlow()

    private val _tasksByMonth = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasksByMonth: StateFlow<List<TaskEntity>> = _tasksByMonth.asStateFlow()

    private val _completedCountByDate = MutableStateFlow(0)
    val completedCountByDate: StateFlow<Int> = _completedCountByDate.asStateFlow()

    private val _completedCountBetween = MutableStateFlow(0)
    val completedCountBetween: StateFlow<Int> = _completedCountBetween.asStateFlow()

    private val _totalOccurrencesBetween = MutableStateFlow(0)
    val totalOccurrencesBetween: StateFlow<Int> = _totalOccurrencesBetween.asStateFlow()

    private val _totalOccurrencesByDate = MutableStateFlow(0)
    val totalOccurrencesByDate: StateFlow<Int> = _totalOccurrencesByDate.asStateFlow()

    private val _taskById = MutableStateFlow<TaskEntity?>(null)
    val taskById: StateFlow<TaskEntity?> = _taskById.asStateFlow()

    private val _allForDate = MutableStateFlow<List<TaskEntity>>(emptyList())
    val allForDate: StateFlow<List<TaskEntity>> = _allForDate.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun loadAllTasks() = viewModelScope.launch {
        _allTasks.value = repo.getAllTasks()
    }

    fun loadTasksByDate(date: String) = viewModelScope.launch {
        _tasksByDate.value = repo.getTasksByDate(date)
    }

    fun loadTasksByMonth(month: String) = viewModelScope.launch {
        _tasksByMonth.value = repo.getTasksByMonth(month)
    }

    fun loadCompletedCountByDate(date: String) = viewModelScope.launch {
        _completedCountByDate.value = repo.getCompletedCountByDate(date)
    }

    fun loadCompletedCountBetween(startDate: String, endDate: String) = viewModelScope.launch {
        _completedCountBetween.value = repo.getCompletedCountBetweenDates(startDate, endDate)
    }

    fun loadTotalOccurrencesBetween(startDate: String, endDate: String) = viewModelScope.launch {
        _totalOccurrencesBetween.value = repo.getTotalTaskOccurrencesBetweenDates(startDate, endDate)
    }

    fun loadTotalOccurrencesByDate(date: String) = viewModelScope.launch {
        _totalOccurrencesByDate.value = repo.getTotalTaskOccurrencesByDate(date)
    }

    fun loadTaskById(taskId: Int) = viewModelScope.launch {
        _taskById.value = repo.getTaskById(taskId)
    }

    fun updateTaskCompletion(taskId: Int, isCompleted: Boolean) = viewModelScope.launch {
        repo.updateTaskCompletion(taskId, isCompleted)
        loadAllTasks()
    }

    fun deleteTask(task: TaskEntity) = viewModelScope.launch {
        repo.deleteTask(task)
        loadAllTasks()
    }

    fun loadAllTasksForDate(date: String) = viewModelScope.launch {
        _allForDate.value = repo.getAllTasksForDate(date)
    }

    fun checkIsCompleted(taskId: Int) = viewModelScope.launch {
        _isCompleted.value = repo.isTaskCompleted(taskId)
    }

    fun addTask(task: TaskEntity) = viewModelScope.launch {
        repo.insertTask(task)
        loadAllTasks()
    }
}
