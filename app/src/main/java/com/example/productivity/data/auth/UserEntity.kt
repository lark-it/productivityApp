package com.example.productivity.data.auth

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.productivity.data.util.Constants

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val xp: Int = 0,
    val lives: Int = Constants.MAX_LIVES,
    @ColumnInfo(name = "level") val level: Int = 1,
    @ColumnInfo(name = "rank") val rank: String = "Новичок"
)

@Entity(tableName = "habit_bonus")
data class HabitBonusEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "habit_id") val habitId: Int,
    @ColumnInfo(name = "week_start") val weekStart: String,
    @ColumnInfo(name = "week_end") val weekEnd: String
)