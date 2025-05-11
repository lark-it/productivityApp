package com.example.productivity.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivity.data.habits.HabitCompletionEntity
import com.example.productivity.data.habits.HabitsEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitsViewModel(
    private val repo: HabitsRepository
) : ViewModel() {
    private val _habits = MutableStateFlow<List<HabitsEntity>>(emptyList())
    val habits: StateFlow<List<HabitsEntity>> = _habits.asStateFlow()

    private val _completions = MutableStateFlow<List<HabitCompletionEntity>>(emptyList())
    val completions: StateFlow<List<HabitCompletionEntity>> = _completions.asStateFlow()

    fun loadHabits() = viewModelScope.launch {
        _habits.value = repo.getAllHabits()
    }

    fun addHabit(habit: HabitsEntity) = viewModelScope.launch {
        repo.insertHabit(habit)
        loadHabits()
    }

    fun toggleHabitCompletion(habitId: Int, isCompleted: Boolean) = viewModelScope.launch {
        repo.updateHabitCompletionFlag(habitId, isCompleted)
        loadHabits()
    }

    fun deleteHabit(habit: HabitsEntity) = viewModelScope.launch {
        repo.deleteHabit(habit)
        loadHabits()
    }

    fun updateHabitDetails(habit: HabitsEntity) = viewModelScope.launch {
        repo.updateHabitDetails(
            habit.id,
            habit.title,
            habit.iconResId,
            habit.color,
            habit.repeatType,
            habit.repeatDays
        )
        loadHabits()
    }

    fun loadCompletions() = viewModelScope.launch {
        _completions.value = repo.getAllCompletions()
    }
    fun addCompletion(completion: HabitCompletionEntity) = viewModelScope.launch {
        repo.insertCompletion(completion)
        loadCompletions()
    }
    fun removeCompletion(habitId: Int, date: String) = viewModelScope.launch {
        repo.deleteCompletion(habitId, date)
        loadCompletions()
    }
}