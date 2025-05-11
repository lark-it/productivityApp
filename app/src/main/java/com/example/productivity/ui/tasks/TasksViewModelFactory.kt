package com.example.productivity.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.productivity.data.tasks.TaskRepository

@Suppress("UNCHECKED_CAST")
class TasksViewModelFactory(
    private val repo: TaskRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
            return TasksViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
