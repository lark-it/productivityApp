package com.example.productivity.home

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val xp: Int = 0
)

@Entity(tableName = "habit_bonus")
data class HabitBonusEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "habit_id") val habitId: Int,
    @ColumnInfo(name = "week_start") val weekStart: String,
    @ColumnInfo(name = "week_end") val weekEnd: String
)