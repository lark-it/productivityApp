package com.example.productivity.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.productivity.data.auth.UserRepository
import com.example.productivity.data.tasks.TaskDao
import com.example.productivity.data.habits.HabitsDao

class MainViewModelFactory(
    private val userRepository: UserRepository,
    private val taskDao: TaskDao,
    private val habitsDao: HabitsDao,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(userRepository, taskDao, habitsDao, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}