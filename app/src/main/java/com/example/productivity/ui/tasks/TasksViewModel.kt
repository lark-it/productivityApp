package com.example.productivity.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.productivity.data.tasks.TaskEntity
import com.example.productivity.data.tasks.TaskRepository

class TasksViewModel(
    private val repo: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks

    init {
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _tasks.value = repo.getAllTasks()
    }

    fun loadByDate(date: String) = viewModelScope.launch {
        _tasks.value = repo.getTasksByDate(date)
    }

    fun add(task: TaskEntity) = viewModelScope.launch {
        repo.insertTask(task)
        loadAll()
    }

    fun update(task: TaskEntity) = viewModelScope.launch {
        repo.insertTask(task) // replace on REPLACE
        loadAll()
    }

    fun delete(task: TaskEntity) = viewModelScope.launch {
        repo.deleteTask(task)
        loadAll()
    }

    fun toggleCompletion(taskId: Int, isCompleted: Boolean) = viewModelScope.launch {
        repo.updateTaskCompletion(taskId, isCompleted)
        loadAll()
    }
}
