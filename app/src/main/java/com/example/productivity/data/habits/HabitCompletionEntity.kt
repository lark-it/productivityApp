package com.example.productivity.data.habits

import androidx.room.Entity

@Entity(tableName = "habit_completion", primaryKeys = ["habitId", "date"])
data class HabitCompletionEntity(
    val habitId: Int,
    val date: String,
    val isCompleted: Boolean,
    val bonusAwarded: Boolean = false
)
