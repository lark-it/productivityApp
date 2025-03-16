package com.example.productivity.home

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 0,
    val xp: Int = 0
)
