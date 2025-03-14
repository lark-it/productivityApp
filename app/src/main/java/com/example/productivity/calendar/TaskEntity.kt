package com.example.productivity.calendar

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.productivity.habits.HabitsEntity

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val date: String,
    val time: String?,
    val importance: Int,
    val isCompleted: Boolean = false
)


