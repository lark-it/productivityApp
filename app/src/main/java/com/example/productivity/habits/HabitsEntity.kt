package com.example.productivity.habits

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "habits")
data class HabitsEntity (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    val title: String,
    val isCompleted:Boolean = false
)
